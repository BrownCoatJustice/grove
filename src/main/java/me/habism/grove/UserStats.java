package me.habism.grove;


/**
 *
 * @author habism
 * Purely a Jackson requirement. Changing this file's fields will break the save file process unless an @ property is mentioned.
 */
public class UserStats {
    //TODO: In later versions implement a User class.
    public int totalTimeFocused;
    public int focusStreak = 0;
    public String lastFocusDate = "";
}
