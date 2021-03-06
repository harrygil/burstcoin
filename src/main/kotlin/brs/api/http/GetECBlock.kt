package brs.api.http

import brs.api.http.common.JSONResponses
import brs.api.http.common.Parameters.TIMESTAMP_PARAMETER
import brs.api.http.common.ResultFields.EC_BLOCK_HEIGHT_RESPONSE
import brs.api.http.common.ResultFields.EC_BLOCK_ID_RESPONSE
import brs.api.http.common.ResultFields.TIMESTAMP_RESPONSE
import brs.objects.Constants.MAX_TIMESTAMP_DIFFERENCE
import brs.services.BlockchainService
import brs.services.EconomicClusteringService
import brs.services.TimeService
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import javax.servlet.http.HttpServletRequest

/**
 * TODO
 */
internal class GetECBlock(
    private val blockchainService: BlockchainService,
    private val timeService: TimeService,
    private val economicClusteringService: EconomicClusteringService
) : APIServlet.JsonRequestHandler(arrayOf(APITag.BLOCKS), TIMESTAMP_PARAMETER) {
    override fun processRequest(request: HttpServletRequest): JsonElement {
        var timestamp = ParameterParser.getTimestamp(request)
        if (timestamp == 0) {
            timestamp = timeService.epochTime
        }
        if (timestamp < blockchainService.lastBlock.timestamp - MAX_TIMESTAMP_DIFFERENCE) {
            return JSONResponses.INCORRECT_TIMESTAMP
        }
        val ecBlock = economicClusteringService.getECBlock(timestamp)
        val response = JsonObject()
        response.addProperty(EC_BLOCK_ID_RESPONSE, ecBlock.stringId)
        response.addProperty(EC_BLOCK_HEIGHT_RESPONSE, ecBlock.height)
        response.addProperty(TIMESTAMP_RESPONSE, timestamp)
        return response
    }
}
