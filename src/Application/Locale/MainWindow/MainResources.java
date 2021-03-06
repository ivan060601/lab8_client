package Application.Locale.MainWindow;

public class MainResources extends java.util.ListResourceBundle {
    private static final Object[][] contents = {
            // BEGINNING OF INTERNATIONALIZATION
            {"Language", "Language"},
            {"Invalid ID", "Invalid ID"},
            {"Wrong birthday format", "Wrong birthday format"},
            {"Enter filter", "Enter filter"},
            {"Server unreachable", "Server unreachable"},
            {"Table", "Table"},
            {"Visualisation", "Visualisation"},
            {"Logout", "Logout"},
            {"No available cities to show", "No available cities to show"}
            // ENDING OF INTERNATIONALIZATION
    };

    @Override
    protected Object[][] getContents() {
        return contents;
    }
}
