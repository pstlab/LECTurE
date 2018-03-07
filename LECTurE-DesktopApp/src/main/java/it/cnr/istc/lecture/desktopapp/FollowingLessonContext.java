/*
 * Copyright (C) 2018 ISTC - CNR
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
package it.cnr.istc.lecture.desktopapp;

import it.cnr.istc.lecture.api.Lesson;
import it.cnr.istc.lecture.api.Lesson.LessonState;
import it.cnr.istc.lecture.api.messages.Event;
import it.cnr.istc.lecture.api.model.LessonModel;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Riccardo De Benedictis
 */
public class FollowingLessonContext {

    private final Lesson lesson;
    private final LessonModel model;
    private final ObjectProperty<LessonState> state = new SimpleObjectProperty<>();
    private final LongProperty time = new SimpleLongProperty(0);
    private final ObservableList<Event> events = FXCollections.observableArrayList();

    FollowingLessonContext(Lesson lesson, LessonModel model) {
        this.lesson = lesson;
        this.model = model;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public LessonModel getModel() {
        return model;
    }

    public ObjectProperty<LessonState> stateProperty() {
        return state;
    }

    public LongProperty timeProperty() {
        return time;
    }

    public ObservableList<Event> eventsProperty() {
        return events;
    }
}
