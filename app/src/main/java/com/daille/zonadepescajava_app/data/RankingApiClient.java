package com.daille.zonadepescajava_app.data;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.os.Handler;
import android.os.Looper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RankingApiClient {

    // ✅ CAMBIA ESTO por tu URL real si difiere
    // (en tus capturas es algo como: https://red-king-7a67ranking-api.matiasdaille.workers.dev)
    private static final String BASE_URL = "https://red-king-7a67ranking-api.matiasdaille.workers.dev";

    private static final int TIMEOUT_MS = 8000;
    private static final ExecutorService IO = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private RankingApiClient() {}

    public interface Callback<T> {
        void onResult(T result, Exception error);
    }

    public static boolean hasInternet(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (cm == null) return false;

            Network net = cm.getActiveNetwork();
            if (net == null) return false;

            NetworkCapabilities caps = cm.getNetworkCapabilities(net);
            if (caps == null) return false;

            // No depender del tipo de transporte (wifi/celular/vpn/etc.).
            // Algunos operadores/reportes usan VPN u otros transports sobre datos móviles.
            if (!caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
                return false;
            }

            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    public static void submitScoreAsync(
            String nombre,
            String pais,
            int puntaje,
            String fechaYYYYMMDD,
            Callback<Boolean> cb
    ) {
        submitScoreAsync(
                nombre, pais, puntaje, fechaYYYYMMDD,
                0, null, null, 0,
                cb
        );
    }

    public static void submitScoreAsync(
            String nombre,
            String pais,
            int puntaje,
            String fechaYYYYMMDD,
            int capturedCount,
            List<String> deckCardIds,
            List<Integer> diceSides,
            int durationMinutes,
            Callback<Boolean> cb
    ) {
        IO.execute(() -> {
            Exception error = null;
            boolean ok = false;

            HttpURLConnection conn = null;
            try {
                URL url = new URL(BASE_URL + "/score");
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");

                JSONObject body = new JSONObject();
                body.put("nombre", nombre);
                body.put("pais", pais);
                body.put("puntaje", puntaje);
                body.put("fecha", fechaYYYYMMDD);

                body.put("capturedCount", capturedCount);
                body.put("durationMinutes", durationMinutes);

                JSONArray deckArr = new JSONArray();
                if (deckCardIds != null) {
                    for (String id : deckCardIds) deckArr.put(id);
                }
                body.put("deckCardIds", deckArr);

                JSONArray diceArr = new JSONArray();
                if (diceSides != null) {
                    for (Integer s : diceSides) diceArr.put(s == null ? 0 : s);
                }
                body.put("diceSides", diceArr);

                byte[] payload = body.toString().getBytes(StandardCharsets.UTF_8);
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(payload);
                }

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
                String resp = readAll(is);

                JSONObject json = new JSONObject(resp);
                ok = json.optBoolean("ok", false);

            } catch (Exception e) {
                error = e;
            } finally {
                if (conn != null) conn.disconnect();
            }


            boolean finalOk = ok;
            Exception finalErr = error;
            MAIN.post(() -> cb.onResult(finalOk, finalErr));
        });
    }


    public static class RemoteScore {
        public final String nombre;
        public final String pais;
        public final int puntaje;
        public final String fecha;
        public final int capturedCount;
        public final List<String> deckCardIds;
        public final List<Integer> diceSides;
        public final int durationMinutes;

        public RemoteScore(String nombre, String pais, int puntaje, String fecha,
                           int capturedCount, List<String> deckCardIds,
                           List<Integer> diceSides, int durationMinutes) {
            this.nombre = nombre;
            this.pais = pais;
            this.puntaje = puntaje;
            this.fecha = fecha;
            this.capturedCount = capturedCount;
            this.deckCardIds = deckCardIds;
            this.diceSides = diceSides;
            this.durationMinutes = durationMinutes;
        }
    }

    public static void fetchTopAsync(int limit, Callback<List<RemoteScore>> cb) {
        IO.execute(() -> {
            Exception error = null;
            List<RemoteScore> out = new ArrayList<>();
            HttpURLConnection conn = null;

            try {
                URL url = new URL(BASE_URL + "/top?limit=" + limit);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(TIMEOUT_MS);
                conn.setReadTimeout(TIMEOUT_MS);

                int code = conn.getResponseCode();
                InputStream is = (code >= 200 && code < 300) ? conn.getInputStream() : conn.getErrorStream();
                String resp = readAll(is);

                JSONObject json = new JSONObject(resp);
                if (json.optBoolean("ok", false)) {
                    JSONArray arr = json.optJSONArray("top");
                    if (arr != null) {
                        for (int i = 0; i < arr.length(); i++) {
                            JSONObject row = arr.getJSONObject(i);
                            JSONArray deckArr = row.optJSONArray("deckCardIds");
                            JSONArray diceArr = row.optJSONArray("diceSides");
                            List<String> deckCardIds = parseStringArray(deckArr);
                            List<Integer> diceSides = parseIntArray(diceArr);
                            out.add(new RemoteScore(
                                    row.optString("nombre", "?"),
                                    row.optString("pais", "?"),
                                    row.optInt("puntaje", 0),
                                    row.optString("fecha", ""),
                                    row.optInt("capturedCount", 0),
                                    deckCardIds,
                                    diceSides,
                                    row.optInt("durationMinutes", 0)
                            ));
                        }
                    }
                } else {
                    error = new RuntimeException("API ok=false: " + json.optString("error"));
                }

            } catch (Exception e) {
                error = e;
            } finally {
                if (conn != null) conn.disconnect();
            }

            Exception finalErr = error;
            MAIN.post(() -> cb.onResult(out, finalErr));
        });
    }

    private static String readAll(InputStream is) throws Exception {
        if (is == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    private static List<String> parseStringArray(JSONArray arr) {
        List<String> out = new ArrayList<>();
        if (arr == null) return out;
        for (int i = 0; i < arr.length(); i++) {
            String value = arr.optString(i, "").trim();
            if (!value.isEmpty()) {
                out.add(value);
            }
        }
        return out;
    }

    private static List<Integer> parseIntArray(JSONArray arr) {
        List<Integer> out = new ArrayList<>();
        if (arr == null) return out;
        for (int i = 0; i < arr.length(); i++) {
            int value = arr.optInt(i, -1);
            if (value > 0) {
                out.add(value);
            }
        }
        return out;
    }
}
