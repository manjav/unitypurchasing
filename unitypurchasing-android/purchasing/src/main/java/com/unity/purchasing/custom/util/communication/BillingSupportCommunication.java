package com.unity.purchasing.custom.util.communication;


import com.unity.purchasing.custom.util.IabResult;

public interface BillingSupportCommunication {
    void onBillingSupportResult(int response);

    void remoteExceptionHappened(IabResult result);
}
