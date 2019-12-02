package brs.services.impl

import brs.services.DeeplinkGeneratorService
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.client.j2se.MatrixToImageConfig
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.awt.image.BufferedImage
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.*

class DeeplinkGeneratorServiceImpl : DeeplinkGeneratorService {
    private val validDomains = arrayOf("payment", "message", "contract", "asset", "market", "generic")

    private val hints = EnumMap<EncodeHintType, ErrorCorrectionLevel>(EncodeHintType::class.java)

    init {
        hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.L
    }

    @Throws(UnsupportedEncodingException::class, IllegalArgumentException::class)
    override fun generateDeepLink(domain: String, action: String?, base64Payload: String?): String {
        validDomains.firstOrNull { d -> d == domain } ?: throw IllegalArgumentException("Invalid domain:$domain")

        val deeplinkBuilder = StringBuilder("burst.")
        deeplinkBuilder.append(domain)
        deeplinkBuilder.append("://")
        deeplinkBuilder.append(Version)
        if (action != null) {
            deeplinkBuilder.append("?action=")
            deeplinkBuilder.append(action)
            if (base64Payload != null) {
                deeplinkBuilder.append("&payload=")
                val encodedPayload = URLEncoder.encode(base64Payload, StandardCharsets.UTF_8.toString())
                require(encodedPayload.length <= MaxPayloadLength) { "Maximum Payload Length ($MaxPayloadLength) exceeded" }
                deeplinkBuilder.append(encodedPayload)
            }
        }
        return deeplinkBuilder.toString()
    }

    @Throws(UnsupportedEncodingException::class, IllegalArgumentException::class, WriterException::class)
    override fun generateDeepLinkQrCode(domain: String, action: String?, base64Payload: String?): BufferedImage {
        return generateQRCode(this.generateDeepLink(domain, action, base64Payload))
    }

    @Throws(WriterException::class)
    private fun generateQRCode(url: String): BufferedImage {
        val qrCodeWriter = QRCodeWriter()
        return MatrixToImageWriter.toBufferedImage(
            qrCodeWriter.encode(url, BarcodeFormat.QR_CODE, 350, 350, hints),
            MatrixToImageConfig()
        )
    }

    companion object {
        private const val Version = "v1"
        private const val MaxPayloadLength = 2048
    }}