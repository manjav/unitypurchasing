#if UNITY_EDITOR
using System.IO;
using UnityEditor;
using UnityEditor.Build;
using UnityEditor.Build.Reporting;
using UnityEngine.Purchasing;

class ManifestWriter : IPreprocessBuildWithReport
{
    public int callbackOrder => 0;

    public void OnPreprocessBuild(BuildReport report)
    {
        // Load BillingMode
        string path = Path.Combine(System.IO.Directory.GetCurrentDirectory(), "Assets", "Resources", "BillingMode.json");
        string billingMode = File.ReadAllText(path);
        if (billingMode == null)
        {
            return;
        }
        StoreConfiguration config = StoreConfiguration.Deserialize(billingMode);

        // Load Manifest Template
        StoreData storeData = StoreData.data.ContainsKey(config.androidStore) ? StoreData.data[config.androidStore] : new StoreData();
        string thisScriptDir = Path.GetDirectoryName(new System.Diagnostics.StackTrace(true).GetFrame(0).GetFileName());
        var manifestTempPath = Path.Combine(thisScriptDir, "AndroidManifestTemp.xml");
        string manifest = File.ReadAllText(manifestTempPath);

        // Replace Manifest 
        manifest = manifest.Replace("___MarketQueries___", storeData.manifestQueries);
        manifest = manifest.Replace("___MarketPermissions___", storeData.manifestPermission);
        manifest = manifest.Replace("___MarketActivities___", storeData.manifestActivity);
        manifest = manifest.Replace("___MarketReceivers___", storeData.manifestReceiver);
        string androidManifestPath = Path.Combine(System.IO.Directory.GetCurrentDirectory(), "Assets", "Plugins", "Android", "AndroidManifest.xml");
        File.WriteAllText(androidManifestPath, manifest);
    }
}
#endif