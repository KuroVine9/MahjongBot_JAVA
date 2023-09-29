package kuro9.mahjongbot.gdrive;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.ServiceAccountCredentials;
import kuro9.mahjongbot.Logger;
import kuro9.mahjongbot.Setting;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class GDrive {

    public static void upload(String fileID, String data_path) {

        CompletableFuture.runAsync(() -> {
            try {
                JSONParser parser = new JSONParser();
                Object obj = parser.parse(new FileReader(Setting.CREDENTIAL_PATH));
                String client_email = ((JSONObject) obj).get("client_email").toString();

                Drive driveService = new Drive.Builder(
                        GoogleNetHttpTransport.newTrustedTransport(),
                        GsonFactory.getDefaultInstance(),
                        new HttpCredentialsAdapter(
                                ServiceAccountCredentials.fromStream(new FileInputStream(Setting.CREDENTIAL_PATH))
                                        .createScoped(Arrays.asList(DriveScopes.DRIVE))
                                        .createDelegated(client_email)
                        )
                ).setApplicationName(client_email.split("@")[0]).build();

                java.io.File fileContent = new java.io.File(data_path);
                FileContent mediaContent = new FileContent("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", fileContent);

                File updateFile = driveService.files().update(fileID, null, mediaContent).execute();
                System.out.print("[MahjongBot:GDrive] Uploading files : ");
                System.out.println(updateFile.getName());
            }
            catch (IOException | GeneralSecurityException e) {
                Logger.addSystemErrorEvent("drive-upload-failure");
            }
            catch (ParseException e) {
                Logger.addSystemErrorEvent("drive-path-undefined");
            }
            catch (Exception e) {
                Logger.addSystemErrorEvent(Logger.UNKNOWN_ISSUE);
                e.printStackTrace();
            }
        });
    }
}
