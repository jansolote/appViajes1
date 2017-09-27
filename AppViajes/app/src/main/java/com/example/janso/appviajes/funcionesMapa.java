package com.example.janso.appviajes;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
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

/**
 * Created by Janso on 27/09/2017.
 */

public class funcionesMapa extends FragmentActivity {

LocationManager locManager;
    LocationListener locListener;
GoogleMap map;

    public funcionesMapa( Context context, Activity activity, GoogleMap map){

        this.map=map;
       locationListener(context,activity,map);

    }

    public GoogleMap getMap() {
        return map;
    }

    public void setMap(GoogleMap map) {
        this.map = map;
    }

    public void locationListener( Context context, Activity activityMetodo, final GoogleMap map){
        locManager=(LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locListener= new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

             setUserLocation(location.getLatitude(),location.getLongitude(),map);
             iniciarBusqueda(location.getLatitude(),location.getLongitude());

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
        }; if(Build.VERSION.SDK_INT<23){

            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locListener);

        }else {

            if(ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){

                ActivityCompat.requestPermissions(activityMetodo,new String[] {Manifest.permission.ACCESS_FINE_LOCATION},1);

            }else {

                locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locListener);

            }


        }




    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION)==PackageManager.PERMISSION_GRANTED){

            locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locListener);


        }

    }

    public void setUserLocation(double lat, double lon, GoogleMap map){
        LatLng latLng= new LatLng(lat,lon);
        map.addMarker(new MarkerOptions().position(latLng).title("USUARIO").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,15));

    }

    public void iniciarBusqueda(double lat,double lon){

        String latitude=String.valueOf(lat);
        String longitude=String.valueOf(lon);

        conexionJson conexion= new conexionJson();
        conexion.execute("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+latitude+","+longitude+"&radius=5000&type=restaurant&keyword=cruise&key=AIzaSyBdLiUmHPXNt5tqS6Yd7t3B-s6AkHbJxHg");
    }

    class conexionJson extends AsyncTask<String,Void,String>{


        @Override
        protected String doInBackground(String... strings) {
            String datosString="";
            try{

                URL url= new URL(strings[0]);
                HttpURLConnection httpURLConnection=(HttpURLConnection) url.openConnection();
                InputStream inputStream= httpURLConnection.getInputStream();
                InputStreamReader reader= new InputStreamReader(inputStream);

                int datos= reader.read();
                while(datos!=-1){

                    char letras=(char)datos;
                    datosString+=letras;

                    datos=reader.read();
                }


            }catch(MalformedURLException ex){

                ex.printStackTrace();

            }catch(IOException ex){

                ex.printStackTrace();

            }

            return datosString;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject objeto= new JSONObject(s);
                Log.i("VALORES JSON: ",objeto.toString());
                String valores=objeto.getString("results");
                Log.i("RESULTS JSON",valores);
                JSONArray arrayResults= new JSONArray(valores);
                for (int i=0;i<arrayResults.length();i++){

                    JSONObject objetoDeArray=arrayResults.getJSONObject(i);
                    String geometryString =objetoDeArray.getString("geometry");
                    String nombresLugares=objetoDeArray.getString("name");

                    Log.i("Nombre Lugares",nombresLugares);

                    JSONObject objetoGeometry = new JSONObject(geometryString);
                    String locationGeometry =objetoGeometry.getString("location");

                    Log.i("LOCATION",locationGeometry);
                    JSONObject objectLocation=new JSONObject(locationGeometry);
                    String lat =objectLocation.getString("lat");
                    String lon= objectLocation.getString("lng");

                    setMapLocation(lat,lon,nombresLugares);

                }


            }catch(JSONException ex){

                ex.printStackTrace();

            }
        }
    }

    public void setMapLocation(String latitude,String longitude,String nombre){
      LatLng markersLocation;
        double lat=Double.parseDouble(latitude);
        double lon=Double.parseDouble(longitude);
     markersLocation=new LatLng(lat,lon);
      map.addMarker(new MarkerOptions().title(nombre).position(markersLocation));


    }
}
