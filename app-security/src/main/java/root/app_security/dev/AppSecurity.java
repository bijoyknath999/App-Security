package root.app_security.dev;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class AppSecurity {

    String linkJson = "https://raw.githubusercontent.com/bijoyknath999/App-Security/refs/heads/main/checker.json";
    private Context context;

    public AppSecurity(Context context) {
        this.context = context;
    }

    public void checkSecurity() {
        String currentPackageName = context.getPackageName();

        // Async task to load the JSON file
        new CheckPackageTask().execute(currentPackageName);
    }

    private class CheckPackageTask extends AsyncTask<String, Void, Boolean> {

        @Override
        protected Boolean doInBackground(String... params) {
            String currentPackageName = params[0];
            boolean isAllowed = false;

            try {

                URL url = new URL(linkJson);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setConnectTimeout(10000); // 10 seconds timeout
                urlConnection.setReadTimeout(10000); // 10 seconds timeout

                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder jsonResponse = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonResponse.append(line);
                }

                // Parse the JSON response
                JSONObject jsonObject = new JSONObject(jsonResponse.toString());
                JSONArray allowedPackages = jsonObject.getJSONArray("allowed_packages");

                // Check if the current package name is in the allowed list
                for (int i = 0; i < allowedPackages.length(); i++) {
                    if (allowedPackages.getString(i).equals(currentPackageName)) {
                        isAllowed = true;
                        break;
                    }
                }

            } catch (Exception e) {
                // Handle any errors (e.g., network issues)
                e.printStackTrace();
                System.out.println(e.getLocalizedMessage());
            }

            return isAllowed;
        }

        @Override
        protected void onPostExecute(Boolean isAllowed) {
            if (!isAllowed) {
                closeApp();
            }
        }
    }

    private void closeApp() {
        if (context instanceof Activity) {
            ((Activity) context).finishAffinity();
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(1);
    }
}
