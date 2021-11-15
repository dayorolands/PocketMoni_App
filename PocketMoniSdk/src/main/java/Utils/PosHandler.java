package Utils;

import android.app.Activity;

public interface PosHandler {
    default void onCVMProcessFinished(Activity activity){};
    default void onEnterPinRequested(Activity activity){};
    default void onDetectICCard(CardReadMode cardType){}
    default void onDetectContactlessCard(CardReadMode cardType){}
    default void onCardTimeount(){}
    default void onCardRemoved(){}
    default void onPinVerificationResult(boolean isSuccess, String message){}
}
