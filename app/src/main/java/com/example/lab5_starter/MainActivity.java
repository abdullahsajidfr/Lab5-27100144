package com.example.lab5_starter;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements CityDialogFragment.CityDialogListener {

    private Button addCityButton;
    private Button deleteCityButton;
    private ListView cityListView;

    private ArrayList<City> cityArrayList;
    private ArrayAdapter<City> cityArrayAdapter;

    private FirebaseFirestore db;
    private CollectionReference citiesRef;
    private int selectedPosition = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set views
        addCityButton = findViewById(R.id.buttonAddCity);
        deleteCityButton = findViewById(R.id.buttonDeleteCity);
        cityListView = findViewById(R.id.listviewCities);

        // create city array
        cityArrayList = new ArrayList<>();
        cityArrayAdapter = new CityArrayAdapter(this, cityArrayList);
        cityListView.setAdapter(cityArrayAdapter);
        cityListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        db = FirebaseFirestore.getInstance();
        citiesRef = db.collection("cities");

        citiesRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Firestore", error.toString());
                    return;
                }
                if (value != null) {
                    cityArrayList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        String city = doc.getId();
                        String province = doc.getString("province");
                        cityArrayList.add(new City(city, province));
                    }
                    cityArrayAdapter.notifyDataSetChanged();
                }
            }
        });

        // set listeners
        addCityButton.setOnClickListener(view -> {
            CityDialogFragment cityDialogFragment = new CityDialogFragment();
            cityDialogFragment.show(getSupportFragmentManager(),"Add City");
        });

        deleteCityButton.setOnClickListener(view -> {
            if (selectedPosition != -1 && selectedPosition < cityArrayList.size()) {
                City cityToDelete = cityArrayList.get(selectedPosition);
                citiesRef.document(cityToDelete.getName())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            Log.d("Firestore", "Document successfully deleted!");
                            selectedPosition = -1;
                            cityListView.clearChoices();
                            cityArrayAdapter.notifyDataSetChanged();
                        })
                        .addOnFailureListener(e -> Log.w("Firestore", "Error deleting document", e));
            }
        });

        cityListView.setOnItemClickListener((adapterView, view, i, l) -> {
            selectedPosition = i;
            // Optionally open dialog for edit
            // City city = cityArrayAdapter.getItem(i);
            // CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
            // cityDialogFragment.show(getSupportFragmentManager(),"City Details");
        });

        cityListView.setOnItemLongClickListener((adapterView, view, i, l) -> {
            City city = cityArrayAdapter.getItem(i);
            CityDialogFragment cityDialogFragment = CityDialogFragment.newInstance(city);
            cityDialogFragment.show(getSupportFragmentManager(),"City Details");
            return true;
        });

    }

    @Override
    public void updateCity(City city, String title, String year) {
        // If name changed, we need to delete old and add new
        if (!city.getName().equals(title)) {
            citiesRef.document(city.getName()).delete();
            city.setName(title);
            city.setProvince(year);
            addCity(city);
        } else {
            city.setProvince(year);
            HashMap<String, Object> data = new HashMap<>();
            data.put("province", city.getProvince());
            citiesRef.document(city.getName()).update(data);
        }
    }

    @Override
    public void addCity(City city){
        HashMap<String, String> data = new HashMap<>();
        data.put("province", city.getProvince());
        citiesRef
                .document(city.getName())
                .set(data)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "DocumentSnapshot successfully written!"))
                .addOnFailureListener(e -> Log.w("Firestore", "Error writing document", e));
    }
}
