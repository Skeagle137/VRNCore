package net.skeagle.vrncore.configurable;

import net.skeagle.vrnlib.config.annotations.Comment;
import net.skeagle.vrnlib.config.annotations.Comments;
import net.skeagle.vrnlib.config.annotations.ConfigName;

public class Settings {

    @Comment("The amount of time (in minutes) between saving playerdata.")
    @ConfigName("auto-save-time-minutes")
    public static int autoSaveInterval = 15;

    //motd
    @Comment("Set to true to let VRNCore handle the motd.")
    @ConfigName("motd-enabled")
    public static boolean motdEnabled = false;
    @Comment("Set to true if the text specified in first-line-text should be shown.")
    @ConfigName("first-line-shown")
    public static boolean firstLineShown = true;
    @Comment("The text for the first line of the motd. Will not show if first-line-shown is false.")
    @ConfigName("first-line-text")
    public static String firstLineText = "My Server";
    @Comments({@Comment("The text for the second line of the motd. If first-line-shown is false,"),
            @Comment("then this text will be the only text shown for the motd."),
            @Comment("This text will have no effect if random-motd is set to true.")})
    @ConfigName("second-line-text")
    public static String secondLineText = "This is a second line";
    @Comment("If true, the text in second-line-text will be replaced with a random line from motds.txt.")
    @Comments({@Comment("If true, the text in second-line-text will be replaced with a random line from motds.txt."),
            @Comment("If motds.txt is empty, this option will be ignored and second-line-text will be used instead.")})
    @ConfigName("random-motd")
    public static boolean randomMotd = true;
    @Comment("The prefix that motds from motds.txt will use.")
    @ConfigName("motd-prefix")
    public static String motdPrefix = "&9";

    //afk
    @Comment("The amount of time, in seconds, before the server marks a player as afk and stops counting their playtime.")
    @ConfigName("marked-afk-seconds")
    public static int afktime = 60;
    @Comment("The amount of time, in seconds, before a player is kicked for being afk too long.")
    @ConfigName("afk-kick-seconds")
    public static int kickTime = 1800;

    //chat
    @Comment("Set to true to let VRNCore handle the chat.")
    @ConfigName("chat-enabled")
    public static boolean chatEnabled = true;
    @Comment("Set to true if players should need the permission \"vrn.chat.allow\" to talk in chat.")
    @ConfigName("chat-allow-permission")
    public static boolean chatPermission = false;
    @Comment("Set to true if players should need the permission \"vrn.chat.color\" to use color and style codes in chat.")
    @ConfigName("chat-color-permission")
    public static boolean colorPermission = true;
    @Comment("The format that all chat messages will show as.")
    @ConfigName("chat-format")
    public static String format = "%prefix%player%suffix: %message";
    @Comment("The format that all player names in the tab list will show as.")
    @ConfigName("list-format")
    public static String listFormat = "%prefix %player %suffix";
    @Comment("If true, players may have multiple prefixes in chat.")
    @ConfigName("allow-multiple-prefixes")
    public static boolean multiplePrefix = true;
    @Comment("If true, players may have multiple suffixes in chat.")
    @ConfigName("allow-multiple-suffixes")
    public static boolean multipleSuffix = true;
    @Comment("If true, nicknames must be unique.")
    @ConfigName("unique-nicknames")
    public static boolean uniqueNicknames = false;
    @Comment("Set to true if the join/leave messages specified in messages be enabled.")
    @ConfigName("join-leave-messages-enabled")
    public static boolean joinLeaveEnabled = true;
    @Comments({@Comment("If true, the welcome message is shown when a player joins the server for the first time."),
            @Comment("Note that join-leave-messages-enabled must be true for this option to have an effect.")})
    @ConfigName("welcome-message-enabled")
    public static boolean welcomeMessageEnabled = true;
    @Comments({@Comment("If true, the return message is shown on join to returning players."),
            @Comment("Note that join-leave-messages-enabled must be true for this option to have an effect.")})
    @ConfigName("return-message-enabled")
    public static boolean returnMessageEnabled = true;

    //homes & warps
    @Comment("The hard limit for the max amount of warps a player can have.")
    @ConfigName("max-amount-warps")
    public static int maxWarps = 100;
    @Comment("The hard limit for the max amount of homes a player can have.")
    @ConfigName("max-amount-homes")
    public static int maxHomes = 100;
    @Comment("Set to true to enable a more efficient, but potentially more performance inducing, method for checking limit permissions.")
    @ConfigName("alternate-limit-permission-check")
    public static boolean alternatePermLimitCheck = false;

    //particles
    @Comment("The amount of time, in seconds, that will pass before idle trail styles will activate.")
    @ConfigName("idle-trail-activation-seconds")
    public static int idleTrailActivation = 8;
}