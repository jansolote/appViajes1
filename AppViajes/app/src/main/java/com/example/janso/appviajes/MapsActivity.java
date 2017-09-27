package com.example.janso.appviajes;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private GoogleMap mMap;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        mapListener();

    }

    public void setUserLocation(double lat,double lon){

        LatLng positionUser=new LatLng(lat,lon);
        mMap.addMarker(new MarkerOptions().position(positionUser).title("Usuario").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(positionUser,15));



    }


    public void iniciarBusqueda(double lat,double lon){

        String latitude=String.valueOf(lat);
        String longitude=String.valueOf(lon);

        conexionJSON conexion = new conexionJSON();

        conexion.execute("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+latitude+","+longitude+"&radius=5000&type=restaurant&keyword=cruise&key=AIzaSyBdLiUmHPXNt5tqS6Yd7t3B-s6AkHbJxHg");



    }



    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        // LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

    }

    public void mapListener(){
        locationManager=(LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener=new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                iniciarBusqueda(location.getLatitude(),location.getLongitude());
                setUserLocation(location.getLatitude(),location.getLongitude());


            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        }; if (Build.VERSION.SDK_INT<23){

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

        }else {

            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){

                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);

            } else{

                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);

            }


        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);


        }

    }

    public void setMapaLocation(String latitude,String longitude,String nombre){
        LatLng markers;
        double lat=Double.parseDouble(latitude);
        double lon=Double.parseDouble(longitude);
        markers=new LatLng(lat,lon);
        mMap.addMarker(new MarkerOptions().title(nombre).position(markers));


    }

    public class conexionJSON extends AsyncTask<String,Void,String> {



        @Override
        protected String doInBackground(String... strings) {

            String valores="";

            try{

                URL url= new URL(strings[0]);
                HttpURLConnection httpURLConnection=(HttpURLConnection) url.openConnection();
                InputStream is=httpURLConnection.getInputStream();
                InputStreamReader ir= new InputStreamReader(is);

                int data=ir.read();
                while(data!=-1){

                    char letras=(char)data;
                    valores+=letras;
                    data=ir.read();


                }


            }catch(MalformedURLException ex){

                ex.printStackTrace();

            }catch(IOException ex){
                ex.printStackTrace();

            }


            return valores;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try{

                JSONObject objeto= new JSONObject(s);

                String results=objeto.getString("results");

                Log.i("DATOS: ",objeto.toString());
                Log.i("RESULTS: ",results);
                JSONArray arr=new JSONArray(results);

                for(int i=0;i<arr.length();i++){

                    JSONObject objetoArray= arr.getJSONObject(i);
                    String objeto2=objetoArray.getString("geometry");
                    String nombreLugares=objetoArray.getString("name");

                    Log.i("NOMBRES: ",nombreLugares);

                    JSONObject objetoGeometry=new JSONObject(objeto2);
                    String locationGeometry=objetoGeometry.getString("location");

                    Log.i("LOCATION",locationGeometry);
                    JSONObject objetoLocation=new JSONObject(locationGeometry);
                    String lat=objetoLocation.getString("lat");
                    Log.i("LATITUDE:",lat);
                    String lon=objetoLocation.getString("lng");
                    Log.i("LONGITUDE",lon);

                    setMapaLocation(lat,lon,nombreLugares);




                  /*   for(int a=0;a<arrayGeometry.length();a++){

                         JSONObject latLangJson=arrayGeometry.getJSONObject(i);
                         String lat=latLangJson.getString("lat");
                         Log.i("LATITUDE",lat);


                     }*/


                    Log.i("NOMBRE",objeto2);
                    Log.i("objGEOMETRY",objetoGeometry.toString());

                }


            }catch(JSONException ex){

                ex.printStackTrace();

            }


        }
    }
}
