package com.example.ecology;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatCallback;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        NavigationView.OnNavigationItemSelectedListener,
        AppCompatCallback {
    private static final String Tag = "MapsActivity";
    private GoogleMap mMap;
    GeoFire geoFire;
    SharedPrefrence mShared;
    public AppCompatDelegate delegate;
    private final float DEFAULT_ZOOM = 15f;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public Boolean mLocationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUESTCODE = 1234;
    public static DatabaseReference mReference;
    DrawerLayout drawerLayout;
    boolean doubleBackToExitPressedOnce = false;
    Toolbar tool;
    FirebaseAuth mAuth;
    ImageView img;
    FirebaseAuth.AuthStateListener authStateListener;
    ActionBarDrawerToggle actionBarDrawerToggle;
    String userID, plantID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        delegate = AppCompatDelegate.create(this, this);
        delegate.onCreate(savedInstanceState);
        delegate.setContentView(R.layout.activity_maps);
        getlocationpermission();
        mShared = new SharedPrefrence(this);
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        img = findViewById(R.id.tree_button);
        mReference = FirebaseDatabase.getInstance().getReference().child("UserData");
        tool = findViewById(R.id.toolbar);
        delegate.setSupportActionBar(tool);
        delegate.getSupportActionBar().setDisplayShowTitleEnabled(true);
        drawerLayout = findViewById(R.id.drawer);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.setDrawerIndicatorEnabled(true);//use for toggling navbar
        actionBarDrawerToggle.syncState();
        NavigationView navigationView = findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);
        img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser() == null) {
                    Toast.makeText(getApplicationContext(), "Logged Out", Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(MapsActivity.this, Login.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                }
            }
        };
        geoFire = new GeoFire(mReference.child(userID));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {


            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }
            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please click again to exit", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else if (id == R.id.feedback) {
            Toast.makeText(getApplicationContext(), "You Clicked this button", Toast.LENGTH_LONG).show();

        } else if (id == R.id.share) {

        } else if (id == R.id.logout) {
            mAuth.signOut();
            startActivity(new Intent(MapsActivity.this, Login.class));
            finish();
        }
        else if(id== R.id.removemarker){
            mMap.clear();
            DatabaseReference remove=mReference.child(userID).child("plant location");
            remove.removeValue();
        }
        return super.onOptionsItemSelected(item);
    }

    void initMap() {
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        supportMapFragment.getMapAsync(MapsActivity.this);
    }

    private void getlocationpermission() {
        String[] permission = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permission, LOCATION_PERMISSION_REQUESTCODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUESTCODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = true;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    initMap();
                }
            }
        }
    }

    private void getDeviceLocationMethod() {
        Log.d(Tag, "getting the device location");
        FusedLocationProviderClient mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        final Task<Location> location = mFusedLocationProviderClient.getLastLocation();
        location.addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull final Task<Location> task) {
                if (task.isSuccessful()) {
                    final DatabaseReference user = FirebaseDatabase.getInstance().getReference("UserData");
                    final Location currentlocation = task.getResult();
                    user.child(userID).child("email").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (!dataSnapshot.exists()) {
                                User_Data user_data = new User_Data(mShared.getUser_pass(), mShared.getUser_email(), currentlocation.getLatitude(),
                                        currentlocation.getLongitude());
                                user.child(userID).child("UserDetails").setValue(user_data);
                                GeoLocation g = new GeoLocation(currentlocation.getLatitude(), currentlocation.getLongitude());
                                geoFire.setLocation("User-Geo-Location", g);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(currentlocation.getLatitude(),
                            currentlocation.getLongitude()), DEFAULT_ZOOM));
                    addMarker(new LatLng(currentlocation.getLatitude(), currentlocation.getLongitude()), mMap);

                } else {
                    Toast.makeText(MapsActivity.this, "Error while finding the location" + task.getException(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void addMarker(LatLng latLng, GoogleMap map) {
        mMap = map;
        int height = 150;
        int width = 150;
        final BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.person);
        Bitmap b = bitmapdraw.getBitmap();
        final Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        MarkerOptions options = new MarkerOptions();
        mMap.addMarker(options.position(latLng).title("Hello World").draggable(true)).
                setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        getDeviceLocationMethod();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        location_to_firebase(googleMap);
    }

    private void location_to_firebase(final GoogleMap google_map) {
        int height = 150;
        int width = 150;
        final BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.icon);
        Bitmap b = bitmapdraw.getBitmap();
        final Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        google_map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                Marker marker = google_map.addMarker(new MarkerOptions().
                        position(point).draggable(true));
                marker.setIcon(BitmapDescriptorFactory
                        .fromBitmap(smallMarker));
                final LatLng latLng = marker.getPosition();
                google_map.addCircle(new CircleOptions().center(latLng).radius(100).strokeColor(Color.TRANSPARENT));
                geoqueries(latLng.latitude, latLng.longitude);
                final DatabaseReference newPost = mReference.child(userID);
                newPost.child("plant location").push().setValue(latLng);

            }
        });
        mReference.child(userID).child("plant location").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (final DataSnapshot s : dataSnapshot.getChildren()) {
                    final LatLng lng = new LatLng(s.child("latitude").getValue(Double.class),
                            s.child("longitude").getValue(Double.class));
                    google_map.addMarker(new MarkerOptions().
                            position(lng).title(s.getKey())).setIcon(BitmapDescriptorFactory.fromBitmap(smallMarker));
                    google_map.addCircle(new CircleOptions().center(lng).radius(100).strokeColor(Color.TRANSPARENT));
                }
                google_map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(final Marker marker) {
                        if (marker != null) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                            builder.setTitle("Alert");
                            builder.setMessage("Are you sure you want to remove this place.");
                            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    plantID = marker.getTitle();
                                    marker.remove();
                                    DatabaseReference dr = mReference.child(userID).child("plant location");
                                    dr.child(plantID).removeValue();
                                }
                            });
                            builder.setNegativeButton("Cancle", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Toast.makeText(MapsActivity.this, "Cancle", Toast.LENGTH_SHORT).show();
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                        return false;
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void geoqueries(double latitude, double longitude) {
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latitude, longitude), 0.5f);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                sendNotification("SAKAR", String.format("%s is entered in the area", key));
            }

            @Override
            public void onKeyExited(String key) {
                sendNotification("Sakar", String.format("%s Hope to see you back", key));
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {

            }

            @Override
            public void onGeoQueryReady() {

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void sendNotification(String sakar, String format) {
        Notification.Builder builder = new Notification.Builder(this).
                setSmallIcon(R.mipmap.ic_launcher_round).
                setContentTitle(sakar).
                setContentText(format);
        NotificationManager manager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
        Intent i = new Intent(this, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notification.defaults |= Notification.DEFAULT_SOUND;
        manager.notify(new Random().nextInt(), notification);
    }//Notification when user entered in the range

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        return false;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu1, menu);
        return true;
    }

    @Override
    public void onSupportActionModeStarted(ActionMode mode) {

    }

    @Override
    public void onSupportActionModeFinished(ActionMode mode) {

    }

    public ActionMode onWindowStartingSupportActionMode(ActionMode.Callback callback) {
        return null;
    }
}
