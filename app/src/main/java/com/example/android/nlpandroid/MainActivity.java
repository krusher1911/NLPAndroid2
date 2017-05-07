package com.example.android.nlpandroid;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Normalizer;

public class MainActivity extends AppCompatActivity {

    Context context = this;
    String operador;
    private TextView textView;
    private EditText editText;
    private String frase;

    public static String removeAccents(String s) {
        s = Normalizer.normalize(s, Normalizer.Form.NFD);
        s = s.replaceAll("[\\p{InCombiningDiacriticalMarks}]", "");
        return s;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.editText);
        textView = (TextView) findViewById(R.id.textView);
    }

    public void nlp(View v) throws JSONException {

        frase = getEditText();
        operador = getOperador(frase);
        String arvore = getArvore(operador);

        String operando1;
        String operando2;
        String resultado;

        if (operador != "erro") {
            operando1 = getOperando(frase, operador, 0);
            operando2 = getOperando(frase, operador, 1);
            resultado = compararOperandos(operando1, operando2, arvore);
            textView.setText(resultado);
        } else {
            //erro
            textView.setText("Não entendi");

        }

    }

    private String getArvore(String operador) {
        if (operador == "é um tipo") {
            return "tipos";
        } else {
            return "partes";
        }
    }

    public String getOperando(String t, String operador, int posicao) {
        String parsedText[] = t.split(operador);
        return parsedText[posicao];
    }

    public String compararOperandos(String o1, String o2, String arvore) throws JSONException {
        String str = LoaderHelper.parseFileToString(context, "Arvore.json");
        JSONObject json = new JSONObject(str);
        JSONArray jsonArray = json.getJSONArray(arvore);

        Elemento e1 = getElemento(jsonArray, o1);
        Elemento e2 = getElemento(jsonArray, o2);

        if (e1.getNome() != null && e2.getNome() != null) {
            if (e1.getMin() >= e2.getMin() && e1.getMax() <= e2.getMax()) {
                return e1.getNome() + " " + operador + " " + e2.getNome() + " (verdadeiro).";
            } else {
                return e1.getNome() + " NÃO " + operador + " " + e2.getNome() + " (falso).";
            }
        } else {
            return "Não foi possível encontrar o(s) operando(s)";
        }

    }

    public String getEditText() {

        return editText.getText().toString();
    }

    public Elemento getElemento(JSONArray jsonArray, String regex) throws JSONException {

        int j = 0;
        Elemento e = new Elemento();
        regex = removeAccents(regex);

        for (int i = 0; i < jsonArray.length(); i++) {

            String obj = jsonArray.getJSONObject(i).getString("nome");
            obj = removeAccents(obj);
            if (regex.contains(obj)) {
                e.setNome(jsonArray.getJSONObject(i).getString("nome"));
                e.setMax(jsonArray.getJSONObject(i).getInt("max"));
                e.setMin(jsonArray.getJSONObject(i).getInt("min"));

            }
        }

        return e;

    }

    public String getOperador(String s) {
        String[] operadores = {"é um tipo", "faz parte"};
        String resultado = "erro";

        for (String oper : operadores) {
            if (s.contains(oper)) {
                resultado = oper;
            }
        }
        return resultado;
    }


}
