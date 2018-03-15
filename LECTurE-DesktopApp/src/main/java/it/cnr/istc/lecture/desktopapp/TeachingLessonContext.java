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
import it.cnr.istc.lecture.api.model.LessonModel;
import java.util.HashMap;
import java.util.Map;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 *
 * @author Riccardo De Benedictis
 */
public class TeachingLessonContext {

    private final Lesson lesson;
    private final LessonModel model;
    private final ObjectProperty<LessonState> state = new SimpleObjectProperty<>(LessonState.Stopped);
    private final LongProperty time = new SimpleLongProperty(0);
    private final ObservableList<TokenRow> tokens = FXCollections.observableArrayList((TokenRow tk) -> new Observable[]{tk.timeProperty()});
    private final Map<Integer, TokenRow> id_tokens = new HashMap<>();

    TeachingLessonContext(Lesson lesson, LessonModel model) {
        this.lesson = lesson;
        this.model = model;
        tokens.addListener((ListChangeListener.Change<? extends TokenRow> c) -> {
            while (c.next()) {
                c.getAddedSubList().forEach((tk) -> id_tokens.put(tk.getId(), tk));
                c.getRemoved().forEach((tk) -> id_tokens.remove(tk.getId()));
            }
        });
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

    public ObservableList<TokenRow> tokensProperty() {
        return tokens;
    }

    public TokenRow getToken(final int id) {
        return id_tokens.get(id);
    }

    public static class TokenRow {

        private final int id;
        private final BooleanProperty executed;
        private final LongProperty time;
        private final LongProperty min;
        private final LongProperty max;
        private final StringProperty name;

        public TokenRow(int id, LongProperty lesson_time, long time, long min, long max, String name) {
            this.id = id;
            this.executed = new SimpleBooleanProperty(false);
            this.time = new SimpleLongProperty(time);
            this.min = new SimpleLongProperty(min);
            this.max = new SimpleLongProperty(max);
            this.name = new SimpleStringProperty(name);
            executed.bind(lesson_time.greaterThanOrEqualTo(this.time));
        }

        public int getId() {
            return id;
        }

        public boolean getExecuted() {
            return executed.get();
        }

        public BooleanProperty executedProperty() {
            return executed;
        }

        public long getTime() {
            return time.get();
        }

        public LongProperty minProperty() {
            return min;
        }

        public LongProperty maxProperty() {
            return max;
        }

        public LongProperty timeProperty() {
            return time;
        }

        public String getName() {
            return name.get();
        }

        public StringProperty nameProperty() {
            return name;
        }
    }
}
