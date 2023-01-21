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
import net.dv8tion.jda.api.requests.RestAction;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;

public class GDrive {

    public static void upload(String fileID, RestAction<net.dv8tion.jda.api.entities.User> admin) {

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

            java.io.File fileContent = new java.io.File(Setting.DATA_PATH);
            FileContent mediaContent = new FileContent("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", fileContent);

            File updateFile = driveService.files().update(fileID, null, mediaContent).execute();
            System.out.println(updateFile);
        } catch (IOException | GeneralSecurityException e) {
            Logger.addSystemErrorEvent("drive-upload-failure", admin);
        } catch (ParseException e) {
            Logger.addSystemErrorEvent("drive-path-undefined", admin);
        }
    }
}
