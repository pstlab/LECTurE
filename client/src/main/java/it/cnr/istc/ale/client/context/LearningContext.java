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
package it.cnr.istc.ale.client.context;

import it.cnr.istc.ale.api.Lesson;
import it.cnr.istc.ale.api.User;
import it.cnr.istc.ale.api.messages.Event;
import it.cnr.istc.ale.api.messages.HideEvent;
import it.cnr.istc.ale.api.messages.QuestionEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 *
 * @author Riccardo De Benedictis
 */
public class LearningContext {

    private static final Logger LOG = Logger.getLogger(LearningContext.class.getName());
    private final Context ctx;
    /**
     * The received events.
     */
    private final ObservableList<Event> events = FXCollections.observableArrayList();
    /**
     * The lessons followed as a student.
     */
    private final ObservableList<Lesson> lessons = FXCollections.observableArrayList();
    /**
     * For each lesson, the received events.
     */
    private final Map<Long, ObservableList<Event>> lesson_events = new HashMap<>();
    /**
     * The followed teachers.
     */
    private final ObservableList<User> teachers = FXCollections.observableArrayList((User u) -> new Observable[]{Context.getContext().connection_ctx.online_users.get(u.getId())});

    LearningContext(Context ctx) {
        this.ctx = ctx;
    }

    void addEvent(Event event) {
        events.add(event);
        lesson_events.get(event.getLessonId()).add(event);
    }

    void hideEvent(HideEvent event) {
        events.removeIf(e -> e.getLessonId() == event.getLessonId() && e.getId() == event.getEventId());
        lesson_events.get(event.getLessonId()).removeIf(e -> e.getId() == event.getEventId());
    }

    public ObservableList<Event> getEvents() {
        return events;
    }

    public ObservableList<Event> getEvents(Lesson lesson) {
        return lesson_events.get(lesson.getId());
    }

    void addLesson(Lesson lesson) {
        lesson_events.put(lesson.getId(), FXCollections.observableArrayList());
        lessons.add(lesson);
    }

    void removeLesson(Lesson lesson) {
        lessons.remove(lesson);
        lesson_events.remove(lesson.getId());
    }

    public ObservableList<Lesson> getLessons() {
        return lessons;
    }

    void addTeacher(User teacher) {
        try {
            ctx.connection_ctx.online_users.put(teacher.getId(), new SimpleBooleanProperty());
            teachers.add(teacher);
            ctx.mqtt.subscribe(teacher.getId() + "/output/on-line", (String topic, MqttMessage message) -> {
                Platform.runLater(() -> ctx.connection_ctx.online_users.get(teacher.getId()).set(Boolean.parseBoolean(new String(message.getPayload()))));
            });
        } catch (MqttException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    void removeTeacher(User teacher) {
        try {
            ctx.mqtt.unsubscribe(teacher.getId() + "/output/on-line");
            teachers.remove(teacher);
            ctx.connection_ctx.online_users.remove(teacher.getId());
        } catch (MqttException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public ObservableList<User> getTeachers() {
        return teachers;
    }

    public void answerQuestion(QuestionEvent question, int answer) {
        ctx.lr.answer_question(question.getLessonId(), question.getId(), answer);
        question.setAnswer(answer);
    }

    void clear() {
        events.clear();
        lessons.clear();
        lesson_events.clear();
        new ArrayList<>(teachers).stream().forEach(teacher -> removeTeacher(teacher));
    }
}
