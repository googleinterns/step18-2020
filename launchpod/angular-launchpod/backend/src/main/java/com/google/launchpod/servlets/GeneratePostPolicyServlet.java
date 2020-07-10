import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.PostPolicyV4;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class GeneratePostPolicyServlet {
  public static void generateSignedPostPolicyV4(String projectId, String bucketName, String blobName) {
    // The ID of your GCP project
    // String projectId = "launchpod-step18-2020";

    // The ID of the GCS bucket to upload to
    // String bucketName = "launchpod-mp3-files"

    // The name to give the object uploaded to GCS
    // String blobName = "your-object-name"
    // this should be the Datastore entity ID of that MP3 object

    Storage storage = StorageOptions.newBuilder().setProjectId(projectId).build().getService();

    PostPolicyV4.PostFieldsV4 fields =
        PostPolicyV4.PostFieldsV4.newBuilder().AddCustomMetadataField("test", "data").build();

    PostPolicyV4 policy =
        storage.generateSignedPostPolicyV4(
            BlobInfo.newBuilder(bucketName, blobName).build(), 10, TimeUnit.MINUTES, fields);

    // send policy to the front end

    // StringBuilder htmlForm =
    //     new StringBuilder(
    //         "<form action='"
    //             + policy.getUrl()
    //             + "' method='POST' enctype='multipart/form-data'>\n");
    // for (Map.Entry<String, String> entry : policy.getFields().entrySet()) {
    //   htmlForm.append(
    //       "  <input name='"
    //           + entry.getKey()
    //           + "' value='"
    //           + entry.getValue()
    //           + "' type='hidden' />\n");
    // }
    // htmlForm.append("  <input type='file' name='file'/><br />\n");
    // htmlForm.append("  <input type='submit' value='Upload File' name='submit'/><br />\n");
    // htmlForm.append("</form>\n");

    // System.out.println(
    //     "You can use the following HTML form to upload an object to bucket "
    //         + bucketName
    //         + " for the next ten minutes:");
    // System.out.println(htmlForm.toString());
  }
}
