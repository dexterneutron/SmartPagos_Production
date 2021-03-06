package com.example.jascaniojah.smartpagos;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.jascaniojah.libraries.DataBaseHandler;
import com.example.jascaniojah.libraries.UserFunctions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class Recarga extends Fragment {
    private static String KEY_SUCCESS = "success";
    private static String KEY_IMEI = "imei";
    private static String KEY_USER = "usuario";
    private static String KEY_ERROR = "Codigo";
    private ArrayList<Productos> productsList;
    Calendar c = Calendar.getInstance();
    SimpleDateFormat df1 = new SimpleDateFormat("dd-MM-yyyy");
    SimpleDateFormat df2 = new SimpleDateFormat("HH:mm");
    SimpleDateFormat df3 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
    TextView numero_recarga,monto,fecha_consulta,id_recarga,resp_fecha,resp_id,registerErrorMsg,resp_hora,conf_numero;
    EditText numero_a_recargar,monto_recarga,resp_conf_numero;
    Button boton_recarga;
    Spinner productsp;
    String producto,telefono,usuario,imei,fecha;
    private String password;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.recarga_frag, container, false);
        fecha_consulta= (TextView) view.findViewById(R.id.fecha_consulta);
        numero_recarga = (TextView) view.findViewById(R.id.numero_recarga);
        monto= (TextView)view.findViewById(R.id.monto);
        conf_numero = (TextView) view.findViewById(R.id.conf_numero);
        resp_conf_numero = (EditText) view.findViewById(R.id.resp_conf_numero);
        id_recarga= (TextView)view.findViewById(R.id.id_recarga);
        resp_fecha=(TextView)view.findViewById(R.id.resp_fecha);
        resp_hora = (TextView) view.findViewById(R.id.resp_hora);
        resp_id=(TextView)view.findViewById(R.id.resp_id);
        numero_a_recargar = (EditText) view.findViewById(R.id.numero_a_recargar);
        monto_recarga = (EditText) view.findViewById(R.id.monto_recarga);
        monto_recarga.addTextChangedListener(tw);
        registerErrorMsg = (TextView) view.findViewById(R.id.recarga_error);



        new getProducts().execute();

        productsList = new ArrayList<Productos>();
        productsp = (Spinner) view.findViewById(R.id.spinner_producto);

        productsp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.WHITE);

                producto = productsList.get(productsp.getSelectedItemPosition()).getCodigo();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        boton_recarga = (Button) view.findViewById(R.id.boton_recarga);
        boton_recarga.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                if (  ( !numero_a_recargar.getText().toString().equals("")) && ( !monto_recarga.getText().toString().equals(""))&& ( !resp_conf_numero.getText().toString().equals("")))
                {

                      if(numero_a_recargar.getText().toString().equals(resp_conf_numero.getText().toString())) {
                          AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                          builder.setMessage("Confirma Venta de Saldo?")
                                  .setCancelable(false)
                                  .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                                      public void onClick(DialogInterface dialog, int id) {
                                          NetAsync();
                                      }
                                  })
                                  .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                      public void onClick(DialogInterface dialog, int id) {
                                          dialog.cancel();
                                      }
                                  });
                          AlertDialog alert = builder.create();
                          alert.show();
                      }else{
                          Toast.makeText(getActivity().getApplicationContext(),
                                  "Los numeros ingresados no son iguales", Toast.LENGTH_SHORT).show();
                      }

                }
                else
                {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Uno de los campos esta vacio", Toast.LENGTH_SHORT).show();
                }

            }
        });



        return view;
    }


    private class getProducts extends AsyncTask<Void, Void, Void> {
        private ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getActivity());
            pDialog.setMessage("Cargando Informacion");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            UserFunctions jsonParser = new UserFunctions();
            TelephonyManager telephonyManager = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
            //telefono=telephonyManager.getLine1Number().toString();
            //telefono = "04142222222";
            DataBaseHandler db = new DataBaseHandler(getActivity().getApplicationContext());
            HashMap cuenta = new HashMap();
            cuenta = db.getUser();
            telefono= cuenta.get("telefono").toString();
            usuario = cuenta.get("usuario").toString();
            imei= cuenta.get("imei").toString();
            password=cuenta.get("password").toString();
            fecha= df3.format(c.getTime());
            JSONObject json = null;
            try {
                json = jsonParser.getProductos(telefono,imei,fecha,usuario,password);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.e("Response: ", "> " + json);

            if (json != null) {
                try {

                    if (json != null) {
                        String red = json.getString(KEY_ERROR);
                        if (Integer.parseInt(red) == 000) {
                            JSONArray banks = json
                                    .getJSONArray("Lista");

                            for (int i = 0; i < banks.length(); i++) {
                                JSONObject catObj = (JSONObject) banks.get(i);
                                Productos cat = new Productos(catObj.getString("Codigo"),
                                        catObj.getString("Nombre"));
                                productsList.add(cat);
                            }
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }else {
                Log.e("JSON Data", "Didn't receive any data from server!");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if (pDialog.isShowing())
                pDialog.dismiss();
            populateSpinner();
        }

    }

    /**
     * Adding spinner data
     * */
    private void populateSpinner() {
        List<String> lables = new ArrayList<String>();

        for (int i = 0; i < productsList.size(); i++) {
            lables.add(productsList.get(i).getNombre());
        }

        // Creating adapter for spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, lables);

        // Drop down layout style - list view with radio button
        spinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        productsp.setAdapter(spinnerAdapter);
    }



    TextWatcher tw = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {

            if (!s.toString().matches("^\\$\\d+$")) {
                String userInput = "" + s.toString().replaceAll("[^\\d]", "");
                StringBuilder cashAmountBuilder = new StringBuilder(userInput);

                monto_recarga.removeTextChangedListener(this);
                monto_recarga.setText(cashAmountBuilder.toString());

                monto_recarga.setTextKeepState("BsF." + cashAmountBuilder.toString().replace(',', '.'));
                Selection.setSelection(monto_recarga.getText(), cashAmountBuilder.toString().length() + 4);

                monto_recarga.addTextChangedListener(this);
            }
        }

    };


    protected class NetCheck extends AsyncTask<String,Void,Boolean>
    {
        private ProgressDialog nDialog;
        protected void  onPreExecute(){
            super.onPreExecute();
            nDialog = new ProgressDialog(getActivity());
            nDialog.setTitle("Checking Network");
            nDialog.setMessage("Loading..");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);
            nDialog.show();

        }
        @Override
        protected Boolean doInBackground(String... args) {
            ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if(netInfo != null && netInfo.isConnected()) {
                try {
                    URL url = new URL("http://www.google.com");
                    HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                    urlc.setConnectTimeout(3000);
                    urlc.connect();
                    if (urlc.getResponseCode() == 200) {
                        return true;
                    }
                } catch (MalformedURLException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return false;
        }

        /**
         * Gets current device state and checks for working internet connection by trying Google.
         **/
        protected void onPostExecute(Boolean th){
            if(th == true){
                nDialog.dismiss();
                new ProcessRecarga().execute();
            }
            else{
                nDialog.dismiss();

            }
        }
    }

    private class ProcessRecarga extends AsyncTask<String,Void,JSONObject> {
        /**
         * Defining Process dialog
         **/
        private ProgressDialog pDialog;
        Float cantidad;
        String fecha_hora,numero,monto,fecha,hora;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
                        numero = numero_a_recargar.getText().toString();
            monto = monto_recarga.getText().toString().replace("BsF.", "");
            fecha_hora = df3.format(c.getTime());
            fecha = df1.format(c.getTime());
            hora = df2.format(c.getTime());
            //cantidad = Float.valueOf(monto);
            pDialog = new ProgressDialog(getActivity());
            pDialog.setTitle("Contactando Servidores");
            pDialog.setMessage("Cargando..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }
        @Override
        protected JSONObject doInBackground(String... args) {
            UserFunctions userFunction = new UserFunctions();
            JSONObject json = null;
            try {
                json = userFunction.registrarVenta(usuario, imei, monto, fecha_hora, numero,producto,password,telefono);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json;
        }
        @Override
        protected void onPostExecute(JSONObject json) {
            /**
             * Checks for success message.
             **/
            try {
                if (json.getString(KEY_ERROR) != null) {
                    registerErrorMsg.setText("");
                    String red = json.getString(KEY_ERROR);
                    if(Integer.parseInt(red) == 000){
                        pDialog.dismiss();

                        numero_a_recargar.getText().clear();
                        monto_recarga.getText().clear();
                        resp_conf_numero.getText().clear();
                        resp_id.setText((CharSequence) json.getString("idrecarga"));
                        resp_fecha.setText(fecha);
                        resp_hora.setText(hora);

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(json.getString("Descripcion_codigo"))
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();

                        /**
                         * Removes all the previous data in the SQlite database
                         **/

                    }
                    else if (Integer.parseInt(red) != 000){
                        pDialog.dismiss();
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage(json.getString("Descripcion_codigo"))
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();


                    }
                }
                else{
                    pDialog.dismiss();
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage("Error de Registro")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();

                       }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }}
    public void NetAsync(){
        new NetCheck().execute();
    }}
