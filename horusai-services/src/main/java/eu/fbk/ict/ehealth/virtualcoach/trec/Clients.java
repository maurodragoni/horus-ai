package eu.fbk.ict.ehealth.virtualcoach.trec;

import eu.fbk.trec.clinical_data.client.ClinicalDataClient;
import eu.fbk.trec.clinical_data.client.ClinicalDataSyncClient;
import eu.fbk.trec.clinical_data.client.SchemaClient;
import eu.fbk.trec.keys.KeysConfig;
import eu.fbk.trec.keys.KeysLoader;
import eu.fbk.trec.personal_data.client.PersonalDataClient;
import eu.fbk.trec.server.correlation.RetrofitFactory;
import eu.fbk.trec.server.gson.GsonFactory;
import eu.fbk.trec.services.ServiceNames;
import eu.fbk.trec.session_manager.client.InternalSessionManagerClient;
import eu.fbk.trec.session_manager.client.InternalSessionManagerClientImpl;
//import eu.fbk.trec.session_manager.model.ServiceLoginRequest;
//import eu.fbk.trec.session_manager.model.TokenTrecJson;
import eu.fbk.trec.signature.SignatureHelperImpl;
import okhttp3.OkHttpClient;
import org.aeonbits.owner.ConfigFactory;
//import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

/**
 * @author Alessandro Valentini
 * Date 1/17/18.
 */
public class Clients {
  
  //private static final Logger logger = LoggerFactory.getLogger(Clients.class);
  
  private static final OkHttpClient okHttpClient = new OkHttpClient.Builder().build();
  private static final ServiceNames serviceNames = ConfigFactory.create(ServiceNames.class, System.getenv());

  private static final Retrofit.Builder retrofitBuilderGson = new RetrofitFactory(okHttpClient, new GsonFactory().provide()).provide();
  private static final Retrofit.Builder retrofitBuilderJackson = new Retrofit.Builder().addConverterFactory(JacksonConverterFactory.create()).client(okHttpClient);

  private static PersonalDataClient personalDataClient = retrofitBuilderGson.baseUrl(serviceNames.getPersonalDataBaseURL()).build().create(PersonalDataClient.class);
  private static ClinicalDataClient clinicalDataClient = retrofitBuilderJackson.baseUrl(serviceNames.getClinicalDataBaseURL()).build().create(ClinicalDataClient.class);
  
  private static SchemaClient schemaClient = retrofitBuilderJackson.baseUrl(serviceNames.getClinicalDataBaseURL()).build().create(SchemaClient.class);
  private static ClinicalDataSyncClient clinicalDataSyncClient = retrofitBuilderJackson.baseUrl(serviceNames.getClinicalDataBaseURL()).build().create(ClinicalDataSyncClient.class);
  private static InternalSessionManagerClient internalSessionManagerClient = initInternalSessionManagerClient();

  private static InternalSessionManagerClient initInternalSessionManagerClient() {
    final KeysConfig keysConfig = ConfigFactory.create(KeysConfig.class, System.getenv());
    final byte[] symmetricKey = new KeysLoader().loadSymmetricKey(keysConfig.getInternalSymmetricKey());

    return new InternalSessionManagerClientImpl(new SignatureHelperImpl(symmetricKey), serviceNames, retrofitBuilderGson);
  }

  public static PersonalDataClient getPersonalDataClient() {
    return personalDataClient;
  }

  public static ClinicalDataClient clinicalDataClient() {
    return clinicalDataClient;
  }

  public static SchemaClient getSchemaClient() {
    return schemaClient;
  }

  public static ClinicalDataSyncClient getClinicalDataSyncClient() {
    return clinicalDataSyncClient;
  }

  public static InternalSessionManagerClient getInternalSessionManagerClient() {
    return internalSessionManagerClient;
  }
}
