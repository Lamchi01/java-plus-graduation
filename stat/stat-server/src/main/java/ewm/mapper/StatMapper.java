package ewm.mapper;

import ewm.ParamHitDto;
import ewm.model.EndpointHit;

public class StatMapper {

    public static EndpointHit toEndpointHit(ParamHitDto paramHitDto) {
        EndpointHit endpointHit = new EndpointHit();
        endpointHit.setApp(paramHitDto.getApp());
        endpointHit.setUri(paramHitDto.getUri());
        endpointHit.setIp(paramHitDto.getIp());
        endpointHit.setTimestamp(paramHitDto.getTimestamp());
        return endpointHit;
    }
}