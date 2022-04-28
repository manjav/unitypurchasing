namespace UnityEngine.Purchasing
{
    /// <summary>
    /// Access Custom store specific functionality.
    /// </summary>
    public class CustomStoreExtensions
    {
        private AndroidJavaObject android;
        /// <summary>
        /// Build the CustomStoreExtensions with the instance of the java object
        /// </summary>
        /// <param name="a">java object</param>
        public CustomStoreExtensions(AndroidJavaObject a)
        {
            this.android = a;
        }
    }
}
