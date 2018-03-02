/*
 * Copyright (C) 2018 Riccardo De Benedictis <riccardo.debenedictis@istc.cnr.it>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.cnr.istc.lecture.webapp.api.messages;

import javax.json.bind.annotation.JsonbCreator;
import javax.json.bind.annotation.JsonbProperty;

/**
 *
 * @author Riccardo De Benedictis
 */
public class HideEvent extends Message {

    private final long lesson_id;
    private final long event_id;

    @JsonbCreator
    public HideEvent(@JsonbProperty("lessonId") long lesson_id, @JsonbProperty("eventId") long event_id) {
        super(MessageType.HideEvent);
        this.lesson_id = lesson_id;
        this.event_id = event_id;
    }

    public long getLessonId() {
        return lesson_id;
    }

    public long getEventId() {
        return event_id;
    }
}
