package eu.fbk.ict.ehealth.virtualcoach.trec;

import eu.fbk.ict.ehealth.virtualcoach.HeLiSConfigurator;
import eu.fbk.ict.ehealth.virtualcoach.VC;
import eu.fbk.trec.exceptions.TrecException;
//import eu.fbk.trec.keys.KeysConfig;
//import eu.fbk.trec.keys.KeysLoader;
//import eu.fbk.trec.server.correlation.RetrofitFactory;
//import eu.fbk.trec.server.gson.GsonFactory;
//import eu.fbk.trec.services.ServiceNames;
//import eu.fbk.trec.session_manager.client.InternalSessionManagerClient;
//import eu.fbk.trec.session_manager.client.InternalSessionManagerClientImpl;
import eu.fbk.trec.session_manager.model.ServiceLoginRequest;
import eu.fbk.trec.session_manager.model.TokenTrecJson;
//import eu.fbk.trec.signature.SignatureHelperImpl;
//import okhttp3.OkHttpClient;
//import org.aeonbits.owner.ConfigFactory;
import retrofit2.Call;
import retrofit2.Response;
//import retrofit2.Retrofit;

import java.io.IOException;
import java.time.LocalDateTime;

import static eu.fbk.ict.ehealth.virtualcoach.trec.Clients.getInternalSessionManagerClient;

/**
 * @author Alessandro Valentini
 *
 * To run locally set environment variables:
 * SESSION_MANAGER_BASE_URL =  https://dockerdev-ehealth.fbk.eu/development/api/session-manager/
 * INTERNAL_KEY_PATH        =  keys/internal_key
 */
public class SessionManager {
  //private static final Logger logger = LoggerFactory.getLogger(TrecLogin.class);

  // Settings
  static HeLiSConfigurator prp = VC.config;
  //private static final String HELIS_USERNAME = "helis";
  private static final String HELIS_USERNAME = prp.getVirtualCoachTreCUser();
  private static final long TTL_MINUTES = 24*60;

  // Local Variable
  private static TokenTrecJson tokenTrecJson = null;
  private static LocalDateTime lastTokenUpdate = null;

  //private static InternalSessionManagerClient internalSessionManagerClient;

  /**
   * Require a new token and store it in tokenTrecJson
   */
  private static void login() {
    // Load internal Keys
    /*final KeysConfig keysConfig = ConfigFactory.create(KeysConfig.class, System.getenv());
    final byte[] symmetricKey = new KeysLoader().loadSymmetricKey(keysConfig.getInternalSymmetricKey());

    // Gson Builder
    final Retrofit.Builder retrofitBuilder = new RetrofitFactory(getOkHttpClient(), new GsonFactory().provide()).provide();

    // Load Service Names
    final ServiceNames serviceNames = ConfigFactory.create(ServiceNames.class, System.getenv());

    // Login client
    internalSessionManagerClient =
      new InternalSessionManagerClientImpl(
        new SignatureHelperImpl(symmetricKey),
        serviceNames,
        retrofitBuilder);
*/
    final Call<TokenTrecJson> internalTestService = getInternalSessionManagerClient().serviceLogin(ServiceLoginRequest.create(HELIS_USERNAME));


    // Get a token using ServiceLogin
    try {
      final Response<TokenTrecJson> serviceLoginRequest = internalTestService.execute();
      if (serviceLoginRequest.isSuccessful()){
        tokenTrecJson = serviceLoginRequest.body();
      } else {
        throw TrecException.serviceUnavailable(
          null, "Received Http Code " + serviceLoginRequest.code() + " as Service Login response");
      }
    } catch (IOException e) {
      throw TrecException.serviceUnavailable(e, "IOException during login");
    }

  }

  /**
   * This method return always a valid TokenTreCJson (which contains both token and trecUserId) requiring a new token
   * only when needed.
   * Please do not cache the token but invoke getTokenTrec() every time.
   *
   * @return TokenTreCJson
   */
  public static TokenTrecJson getTokenTrec() {
    if ((tokenTrecJson == null) || (lastTokenUpdate.plusMinutes(TTL_MINUTES-1).isBefore(LocalDateTime.now()))){
      login();
      lastTokenUpdate = LocalDateTime.now();
    }
    return tokenTrecJson;
  }
}
