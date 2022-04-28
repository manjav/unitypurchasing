namespace UnityEngine.Purchasing
{
    /// <summary>
    /// Access Myket store specific functionality.
    /// </summary>
    public class MyketStoreExtensions
	{
		private AndroidJavaObject android;
		/// <summary>
		/// Build the MyketStoreExtensions with the instance of the Myket java object
        /// </summary>
		/// <param name="a">Myket java object</param>
        public MyketStoreExtensions(AndroidJavaObject a) {
			this.android = a;
		}
	}
}
