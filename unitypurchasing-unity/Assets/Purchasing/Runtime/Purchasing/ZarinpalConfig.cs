using System;

namespace UnityEngine.Purchasing
{   /// <summary>
    /// Define price, merchantId, description for Zarinpal store.
    /// </summary>
    [Serializable]
    public class ZarinpalConfig
    {

        /// <summary>
        /// The price as a decimal.
        /// </summary>
        public int price;

        /// <summary>
        /// The merchantId as a string.
        /// </summary>
        public string merchantId;

        /// <summary>
        /// The description as a string.
        /// </summary>
        public string description;

        internal bool IsEmpty()
        {
            return string.IsNullOrWhiteSpace(merchantId);
        }
    }
}