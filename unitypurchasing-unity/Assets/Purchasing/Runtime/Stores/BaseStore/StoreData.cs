using System.IO;
using System.Collections.Generic;

namespace UnityEngine.Purchasing
{
    public class StoreData
    {
        public string storePackageName;
        public string bindURL;
        public string manifestPermission;

        public string manifestQueries;
        public string manifestActivity;
        public string manifestReceiver;
        public string downloadURL = "";
        public List<string> aarFiles;
        public static Dictionary<AppStore, StoreData> data = new Dictionary<AppStore, StoreData>
        {
            {
                AppStore.AmazonAppStore, new StoreData {
                    aarFiles = new List<string>{"AmazonAppStore"}
                }
            },
            {
                AppStore.Cafebazaar, new StoreData {
            downloadURL =   "https://cafebazaar.ir/app/",
        storePackageName =  "com.farsitel.bazaar",
                bindURL =   "ir.cafebazaar.pardakht.InAppBillingService.BIND",
    manifestPermission =    "\n  <uses-permission android:name=\"com.farsitel.bazaar.permission.PAY_THROUGH_BAZAAR\" />\n",
        manifestQueries =   "\n  <queries> <package android:name=\"com.farsitel.bazaar\" /> </queries>",
        manifestActivity =  "\n    <activity"+
                            "\n      android:name=\"com.unity.purchasing.custom.util.ProxyBillingActivity\""+
                            "\n      android:theme=\"@android:style/Theme.Translucent.NoTitleBar.Fullscreen\""+
                            "\n      android:exported=\"true\"/>",
        manifestReceiver =  "\n    <receiver" +
                            "\n      android:name=\"com.unity.purchasing.custom.util.IABReceiver\" android:exported=\"true\">" +
                            "\n      <intent-filter>" +
                            "\n        <action android:name=\"com.farsitel.bazaar.ping\" />" +
                            "\n        <action android:name=\"com.farsitel.bazaar.purchase\" />" +
                            "\n        <action android:name=\"com.farsitel.bazaar.getPurchase\" />" +
                            "\n        <action android:name=\"com.farsitel.bazaar.billingSupport\" />" +
                            "\n        <action android:name=\"com.farsitel.bazaar.skuDetail\" />" +
                            "\n        <action android:name=\"com.farsitel.bazaar.consume\" />" +
                            "\n      </intent-filter>" +
                            "\n    </receiver>",
                aarFiles = new List<string>{ "purchasing-release" }
                }
            },
            {
                AppStore.Myket, new StoreData {
            downloadURL =   "https://myket.ir/app/",
        storePackageName =  "ir.mservices.market",
                bindURL =   "ir.mservices.market.InAppBillingService.BIND",
    manifestPermission =    "\n  <uses-permission android:name=\"ir.mservices.market.BIND\" />\n",
        manifestQueries =   "\n  <queries>" +
                            "\n    <package android:name=\"ir.mservices.market\" />"+
                            "\n    <intent>"+
                            "\n      <action android:name=\"ir.mservices.market.InAppBillingService.BIND\" />"+
                            "\n      <data android:mimeType=\"*/*\" />"+
                            "\n    </intent>"+
                            "\n  </queries>",
        manifestActivity =  "\n    <activity"+
                            "\n      android:name=\"com.unity.purchasing.custom.util.ProxyBillingActivity\""+
                            "\n      android:theme=\"@android:style/Theme.Translucent.NoTitleBar.Fullscreen\""+
                            "\n      android:exported=\"true\"/>",
        manifestReceiver =  "\n    <receiver" +
                            "\n      android:name=\"com.unity.purchasing.custom.util.IABReceiver\" android:exported=\"true\">" +
                            "\n      <intent-filter>" +
                            "\n        <action android:name=\"ir.mservices.market.ping\" />" +
                            "\n        <action android:name=\"ir.mservices.market.purchase\" />" +
                            "\n        <action android:name=\"ir.mservices.market.getPurchase\" />" +
                            "\n        <action android:name=\"ir.mservices.market.billingSupport\" />" +
                            "\n        <action android:name=\"ir.mservices.market.skuDetail\" />" +
                            "\n        <action android:name=\"ir.mservices.market.consume\" />" +
                            "\n      </intent-filter>" +
                            "\n    </receiver>",
                aarFiles = new List<string>{ "purchasing-release" }
                }
            },
            {
                AppStore.GooglePlay, new StoreData {
                downloadURL = "https://play.google.com/store/apps/details?id=" ,
                aarFiles = new List<string>{ "billing-3.0.3" }
                }
            },
            {
                AppStore.Zarinpal, new StoreData {
                storePackageName = "zarinpal",
                bindURL = "",
                downloadURL = "https://play.google.com/store/apps/details?id=",
                aarFiles = new List<string>{ "purchasing-release" }
                }
            }
        };

        public static AppStore LoadStore()
        {
#if UNITY_EDITOR
            string path = Path.Combine(System.IO.Directory.GetCurrentDirectory(), "Assets", "Resources", "BillingMode.json");
            string billingMode = File.ReadAllText(path);
            if (billingMode == null)
            {
                return AppStore.NotSpecified;
            }
            return StoreConfiguration.Deserialize(billingMode).androidStore;
#endif
            var textAsset = (Resources.Load("BillingMode") as TextAsset);
            StoreConfiguration config = null;
            if (null != textAsset)
            {
                config = StoreConfiguration.Deserialize(textAsset.text);
            }
            return config.androidStore;
        }
    }
}