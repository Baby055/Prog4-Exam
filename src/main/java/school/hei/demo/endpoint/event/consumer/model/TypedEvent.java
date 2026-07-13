package school.hei.demo.endpoint.event.consumer.model;

import school.hei.demo.PojaGenerated;
import school.hei.demo.endpoint.event.model.PojaEvent;

@PojaGenerated
public record TypedEvent(String typeName, PojaEvent payload) {}
