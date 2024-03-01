package com.wcube.paroyalty.receiver

class IBeacon {
        private var uuid: String = ""

        /**
         * The measured signal strength of the Bluetooth packet
         */
        private var rssi: Int = 0

        /**
         * packet raw data
         */
        private var rawByteData: ByteArray = ByteArray(30)

        /**
         * major minor, and their position (based on iBeacon specs)
         */
        private var major: Int? = null
        private val majorPosStart = 25
        private val majorPosEnd = 26

        private var minor: Int? = null
        private val minorPosStart = 27
        private val minorPosEnd = 28

        private val TAG = "IBeacon"

        constructor(packetData: ByteArray) {
            rawByteData = packetData
        }

        private fun bytesToHex(bytes: ByteArray): String {
            val hexArray = "0123456789ABCDEF".toCharArray()
            val hexChars = CharArray(bytes.size * 2)
            for (j in bytes.indices) {
                val v: Int = bytes[j].toInt() and 0xFF
                hexChars[j * 2] = hexArray.get(v ushr 4)
                hexChars[j * 2 + 1] = hexArray.get(v and 0x0F)
            }
            return String(hexChars)
        }

        /**
         * Parse iBeacon UUID from packet
         */
        private fun parseUUID() {
            var startByte = 2
            while (startByte <= 5) {
                if (rawByteData[startByte + 2].toInt() and 0xff == 0x02 && rawByteData[startByte + 3].toInt() and 0xff == 0x15) {
                    val uuidBytes = ByteArray(16)
                    System.arraycopy(rawByteData, startByte + 4, uuidBytes, 0, 16)
                    val hexString = bytesToHex(uuidBytes)
                    if (!hexString.isNullOrEmpty()) {
                        uuid = hexString.substring(0, 8) + "-" +
                                hexString.substring(8, 12) + "-" +
                                hexString.substring(12, 16) + "-" +
                                hexString.substring(16, 20) + "-" +
                                hexString.substring(20, 32)
                        return
                    }
                }
                startByte++
            }
        }

        fun getUUID(): String {
            if (uuid.isNullOrEmpty()) {
                parseUUID()
            }
            return uuid
        }

        fun getMajor(): Int {
            if (major == null)
                major = (rawByteData[majorPosStart].toInt() and 0xff) * 0x100 + (rawByteData[majorPosEnd].toInt() and 0xff)
            return major as Int
        }

        fun getMinor(): Int {
            if (minor == null)
                minor = (rawByteData[minorPosStart].toInt() and 0xff) * 0x100 + (rawByteData[minorPosEnd].toInt() and 0xff)
            return minor as Int
        }
}