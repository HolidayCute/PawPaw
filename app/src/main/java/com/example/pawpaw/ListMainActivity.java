package com.example.pawpaw;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ListMainActivity extends AppCompatActivity {

    ListView listView;
    List<String> locationNames = new ArrayList<>();
    List<String> locationImages = new ArrayList<>();
    Button back;

    FirebaseStorage storage = FirebaseStorage.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_main);

        //How to use get method sample:
        //Set id to search in Database
        String locationId ="ab";
        Log.w("lma","count1");

        listView = findViewById(R.id.listview);

        //To get data from database
        //A list to store all the results from database since a location can have several reviews
        final List<Reviews> result = new ArrayList<>();

        //Initiate the real firestore database
        final FirebaseFirestore database = FirebaseFirestore.getInstance();

        final LatLng currentLocation = new LatLng(43.0712741,-89.3911507);
        String userID1 = "Eileen";
        DocumentReference docRef = database.collection("friends").document(userID1);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                final Friend friend = documentSnapshot.toObject(Friend.class);

                final ArrayList<com.example.pawpaw.Location> result= new ArrayList<>();
                //TODO: Call the function which uses friend info from the other class
                database.collection("locations")
                        .whereLessThan("longitude", currentLocation.longitude + 1.0)
                        .whereGreaterThan("longitude", currentLocation.longitude-1.0)
                        .get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        //Put all results that we get from database in result list
                                        result.add(document.toObject(com.example.pawpaw.Location.class));

                                        Log.d("ListMainActivity", document.getId() + " => " + document.getData());
                                    }
                                    Log.d("ListMainActivity", String.valueOf(result.size()));

                                    double a;
                                    double b;
                                    for(int i = 0 ;i<result.size();i++) {
                                        com.example.pawpaw.Location lo = result.get(i);
                                        LatLng lc = new LatLng(result.get(i).getLatitude(),result.get(i).getLongitude());
                                        Log.d("MapHomePage", lc.longitude +","+lc.latitude);
                                        a = lc.latitude;
                                        b = lc.longitude;
                                        if (currentLocation.latitude-1.0<= a && currentLocation.latitude+1.0>= a ){
                                            for (int j = 0; j<result.get(i).getReviewedUsers().size();j++){
                                                for (int k = 0; k<friend.getUser2IDs().size();k++){
                                                    if (friend.getUser2IDs().get(k).equals(result.get(i).getReviewedUsers().get(j))){
                                                        locationNames.add(result.get(i).getLocationName());
                                                        locationImages.add(result.get(i).getPhotos().get(j));
                                                    }
                                                }

                                            }
                                        }

                                        back= findViewById(R.id.button4);
                                        back.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                startActivity(new Intent(ListMainActivity.this, MapHomePage.class));
                                            }
                                        });
                                        ListMainActivity.CursorAdapter cursorAdapter = new ListMainActivity.CursorAdapter();

                                        listView.setAdapter(cursorAdapter);
                                    }
                                    Log.w("ListMainActivity", "added");
                                } else {
                                    Log.d("ListMainActivity", "Error getting documents: ", task.getException());
                                }
                            }
                        });


                Log.d("ListMainActivity", "Successfully get friends from database");
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int i, long id) {
                Intent intent = new Intent(getApplicationContext(),ListdataActivity.class);
                intent.putExtra("name",locationNames.get(i));
                intent.putExtra("image", locationImages.get(i));
                startActivity(intent);

            }
        });

        Log.w("lma","count3");

    }




    private class CursorAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return locationNames.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int i, View convertView, ViewGroup parent) {
            View view1 = getLayoutInflater().inflate(R.layout.row_data,null);
            TextView name = view1.findViewById(R.id.fruits);
            ImageView image = view1.findViewById(R.id.images);

            name.setText(locationNames.get(i));

            // Get String data from Intent
            String locationAddress = "images/"+locationImages.get(i);

            //Display image
            StorageReference storageRef = storage.getReference();

            storageRef.child(locationAddress).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    helper(uri.toString());
                    // Got the download URL for the image
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });



            return view1;
        }

        //Helper method to display image
        private void helper(String uri){
            View view1 = getLayoutInflater().inflate(R.layout.row_data,null);
            ImageView image = view1.findViewById(R.id.images);

            ImageLoadAsyncTask imageLoadAsyncTask = new ImageLoadAsyncTask(uri, image);
            imageLoadAsyncTask.execute();

        }
    }



}
