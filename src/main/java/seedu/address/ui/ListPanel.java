package seedu.address.ui;

import java.util.logging.Logger;

import com.google.common.eventbus.Subscribe;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.Region;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.events.model.UserDataChangedEvent;
import seedu.address.commons.events.model.UserLoggedOutEvent;
import seedu.address.commons.events.ui.JumpToListRequestEvent;
import seedu.address.commons.events.ui.PanelSelectionChangedEvent;
import seedu.address.model.accounting.Debt;
import seedu.address.model.group.Friendship;
import seedu.address.model.group.Group;
import seedu.address.model.jio.Jio;
import seedu.address.model.restaurant.Restaurant;

/**
 * Panel containing the list of user data.
 */
public class ListPanel<T> extends UiPart<Region> {
    private static final String FXML = "ListPanel.fxml";
    private final Logger logger = LogsCenter.getLogger(ListPanel.class);
    private String type;

    @javafx.fxml.FXML
    private ListView<T> listView;

    public ListPanel(ObservableList<T> list) {
        super(FXML);
        setConnections(list);
        registerAsAnEventHandler(this);
    }

    private void setConnections(ObservableList<T> list) {
        listView.setItems(list);
        listView.setCellFactory(listView -> new ListViewCell());
        setEventHandlerForSelectionChangeEvent();
    }

    private void setEventHandlerForSelectionChangeEvent() {
        listView.getSelectionModel().selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        logger.fine("Selection in restaurant list panel changed to : '" + newValue + "'");
                        raise(new PanelSelectionChangedEvent(newValue));
                    }
                });
    }

    @Subscribe
    private void handleUserDataChangedEvent(UserDataChangedEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        if (type == "Jio") {
            listView.setItems((ObservableList<T>) event.data.getJios());
        }
        if (type == "Debt") {
            listView.setItems((ObservableList<T>) event.user.getDebts());
        }
        if (type == "Group") {
            listView.setItems((ObservableList<T>) FXCollections.observableArrayList(event.user.getGroups()));
        }
    }

    @Subscribe
    private void handleUserLoggedOutEvent(UserLoggedOutEvent event) {
        listView.setItems(null);
    }


    /**
     * Scrolls to the {@code Card} at the {@code index} and selects it.
     */
    private void scrollTo(int index) {
        Platform.runLater(() -> {
            listView.scrollTo(index);
            listView.getSelectionModel().clearAndSelect(index);

        });
    }

    @Subscribe
    private void handleJumpToListRequestEvent(JumpToListRequestEvent event) {
        logger.info(LogsCenter.getEventHandlingLogMessage(event));
        scrollTo(event.targetIndex);
    }

    /**
     * Custom {@code ListCell} that displays the graphics of a {@code Restaurant} using a {@code RestaurantCard}.
     */
    class ListViewCell<T> extends ListCell<T> {
        @Override
        protected void updateItem(T item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setGraphic(null);
                setText(null);
            } else {
                if (item instanceof Restaurant) {
                    setGraphic(new RestaurantCard((Restaurant) item, getIndex() + 1).getRoot());
                    type = "Restaurant";
                }
                if (item instanceof Jio) {
                    setGraphic(new JioCard((Jio) item, getIndex() + 1).getRoot());
                    type = "Jio";
                }
                if (item instanceof Group) {
                    setGraphic(new GroupCard((Group) item, getIndex() + 1).getRoot());
                    type = "Group";
                }

                if (item instanceof Friendship) {
                    setGraphic(new GroupCard((Group) item, getIndex() + 1).getRoot());
                    type = "Group";
                }

                if (item instanceof Debt) {
                    setGraphic(new DebtCard((Debt) item, getIndex() + 1).getRoot());
                    type = "Debt";
                }
            }
        }
    }

}


