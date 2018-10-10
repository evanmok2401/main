package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.ComponentManager;
import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.events.model.AddressBookChangedEvent;
import seedu.address.commons.events.model.UserDataChangedEvent;
import seedu.address.model.restaurant.Restaurant;
import seedu.address.model.user.Friendship;
import seedu.address.model.user.FriendshipStatus;
import seedu.address.model.user.Password;
import seedu.address.model.user.User;
import seedu.address.model.user.Username;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager extends ComponentManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);

    private final VersionedAddressBook versionedAddressBook;
    private final FilteredList<Restaurant> filteredRestaurants;
    private UserData userData;
    private boolean isLoggedIn = false;
    private User currentUser = null;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, UserPrefs userPrefs) {
        super();
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        versionedAddressBook = new VersionedAddressBook(addressBook);
        filteredRestaurants = new FilteredList<>(versionedAddressBook.getRestaurantList());
    }

    public ModelManager(ReadOnlyAddressBook addressBook, UserPrefs userPrefs,
                        UserData userData) {
        super();
        requireAllNonNull(addressBook, userPrefs, userData);

        logger.fine("Initializing with address book: " + addressBook + " and user prefs " + userPrefs);

        versionedAddressBook = new VersionedAddressBook(addressBook);
        filteredRestaurants = new FilteredList<>(versionedAddressBook.getRestaurantList());
        this.userData = userData;
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== Model Manager Miscellaneous Methods =+==========================================================

    public boolean isCurrentlyLoggedIn() {
        return this.isLoggedIn;
    }

    @Override
    public void resetData(ReadOnlyAddressBook newData) {
        versionedAddressBook.resetData(newData);
        indicateAddressBookChanged();
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return versionedAddressBook;
    }

    /** Raises an event to indicate the model has changed */
    private void indicateAddressBookChanged() {
        raise(new AddressBookChangedEvent(versionedAddressBook));
    }

    /** Raises an event to indicate the model has changed */
    private void indicateUserDataChanged() {
        raise(new UserDataChangedEvent(userData));
    }

    //=========== Model Manager Restaurants Methods =+============================================================

    @Override
    public boolean hasRestaurant(Restaurant restaurant) {
        requireNonNull(restaurant);
        return versionedAddressBook.hasRestaurant(restaurant);
    }

    @Override
    public void deleteRestaurant(Restaurant target) {
        versionedAddressBook.removeRestaurant(target);
        indicateAddressBookChanged();
    }

    @Override
    public void addRestaurant(Restaurant restaurant) {
        versionedAddressBook.addRestaurant(restaurant);
        updateFilteredRestaurantList(PREDICATE_SHOW_ALL_RESTAURANTS);
        indicateAddressBookChanged();
    }

    @Override
    public void updateRestaurant(Restaurant target, Restaurant editedRestaurant) {
        requireAllNonNull(target, editedRestaurant);

        versionedAddressBook.updateRestaurant(target, editedRestaurant);
        indicateAddressBookChanged();
    }

    //=========== Filtered Restaurant List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Restaurant} backed by the internal list of
     * {@code versionedAddressBook}
     */
    @Override
    public ObservableList<Restaurant> getFilteredRestaurantList() {
        return FXCollections.unmodifiableObservableList(filteredRestaurants);
    }

    @Override
    public void updateFilteredRestaurantList(Predicate<Restaurant> predicate) {
        requireNonNull(predicate);
        filteredRestaurants.setPredicate(predicate);
    }

    //=========== Model Manager User Methods =+===================================================================

    @Override
    public boolean hasUser(Username username) {
        requireNonNull(username);
        return userData.hasUser(username);
    }

    @Override
    public boolean verifyLogin(Username username, Password password) {
        requireAllNonNull(username);
        requireAllNonNull(password);
        return userData.verifyLogin(username, password);
    }

    @Override
    public void addUser(User user) {
        userData.addUser(user);
        indicateUserDataChanged();
    }

    @Override
    public void loginUser(User user) {
        requireAllNonNull(user);
        this.currentUser = user;
        this.isLoggedIn = true;
    }

    @Override
    public void loginUser(Username username) {
        requireAllNonNull(username);
        User userToLogin = userData.getUsernameUserHashMap().get(username);
        requireAllNonNull(userToLogin);
        this.currentUser = userToLogin;
        this.isLoggedIn = true;
    }

    @Override
    public void logoutUser() {
        this.currentUser = null;
        this.isLoggedIn = false;
    }

    @Override
    public boolean hasUsernameFriendRequest(Username friendUsername) {
        User friendUser = userData.getUser(friendUsername);
        return currentUser.getFriendRequests()
                .contains(new Friendship(friendUser, currentUser, currentUser));
    }

    @Override
    public boolean hasUsernameFriend(Username friendUsername) {
        User friendUser = userData.getUser(friendUsername);
        return currentUser.getFriends()
                .contains(new Friendship(friendUser, currentUser, currentUser));
    }

    @Override
    public void addFriend(Username friendUsername) {
        User friendUser = userData.getUser(friendUsername);
        Friendship friendship = new Friendship(friendUser, currentUser, currentUser, FriendshipStatus.PENDING);
        currentUser.addFriend(friendship);
        indicateUserDataChanged();
    }


    //=========== Undo/Redo/Commit ===============================================================================

    @Override
    public boolean canUndoAddressBook() {
        return versionedAddressBook.canUndo();
    }

    @Override
    public boolean canRedoAddressBook() {
        return versionedAddressBook.canRedo();
    }

    @Override
    public void undoAddressBook() {
        versionedAddressBook.undo();
        indicateAddressBookChanged();
    }

    @Override
    public void redoAddressBook() {
        versionedAddressBook.redo();
        indicateAddressBookChanged();
    }

    @Override
    public void commitAddressBook() {
        versionedAddressBook.commit();
    }

    @Override
    public boolean equals(Object obj) {
        // short circuit if same object
        if (obj == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(obj instanceof ModelManager)) {
            return false;
        }

        // state check
        ModelManager other = (ModelManager) obj;
        return versionedAddressBook.equals(other.versionedAddressBook)
                && filteredRestaurants.equals(other.filteredRestaurants);
    }

}
