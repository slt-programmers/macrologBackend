package csl.database;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseUpdater {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseUpdater.class);

    public DatabaseUpdater() {}

    public static void updateDatabaseDropFoodColumns() throws SQLException {

        final String COL_UNIT_NAME = "unit_name";
        final String COL_UNIT_GRAMS = "unit_grams";
        final String COL_MEASUREMENT = "measurement";

        String[] sql = new String[] {
                "ALTER TABLE " + FoodRepository.TABLE_NAME + " DROP COLUMN " + COL_MEASUREMENT,
                "ALTER TABLE " + FoodRepository.TABLE_NAME + " DROP COLUMN " + COL_UNIT_GRAMS,
                "ALTER TABLE " + FoodRepository.TABLE_NAME + " DROP COLUMN " + COL_UNIT_NAME
        };

        runStatements(sql);
    }

//    public static void updateDatabaseDropColumns() throws SQLException {
//        String sql = "ALTER TABLE " + PortionRepository.TABLE_NAME + " DROP COLUMN " + PortionRepository.COL_UNIT_MULTIPLIER;
//        String[] sqlArray = new String[]{sql};
//        runStatements(sqlArray);
//    }

    public static void updateRefactoreMeasurement() throws SQLException {
//        String foodsql = "SELECT * FROM food WHERE measurement = 'UNIT' and user_id = 2";
//        String logentrySql = "SELECT * FROM logentry WHERE food_id IN(SELECT id FROM food WHERE measurement = 'UNIT' AND user_id = 2)" ;
//
//        FoodRepository foodrepo = new FoodRepository();
//        PortionRepository portionrepo = new PortionRepository();
//        LogEntryRepository entryrepo = new LogEntryRepository();
//
//        List<Food> foodlist = foodrepo.getSomeFood(foodsql);
//        for (Food food : foodlist){
//            food.setProtein(food.getProtein() / food.getUnitGrams() * 100);
//            food.setFat(food.getFat() / food.getUnitGrams() * 100);
//            food.setCarbs(food.getCarbs() / food.getUnitGrams() * 100);
//
//            Portion portion = new Portion();
//            portion.setDescription(food.getUnitName());
//            food.setUnitName("gram");
//
//            portion.setGrams(food.getUnitGrams());
//            food.setUnitGrams(100.00);
//
//            foodrepo.updateFood(2, food);
//            portionrepo.addPortion(food.getId(), portion);
//        }
//
//        List<LogEntry> entries = entryrepo.getSomeLogEntries(logentrySql);
//        for (LogEntry entry : entries) {
//            List<Portion> portions = portionrepo.getPortions(entry.getFoodId());
//            Portion portion = portions.get(0);
//            entry.setPortionId(portion.getId());
//            entryrepo.updateLogEntry(2, entry);
//        }
    }

    public static void updateAT() {
        String[] statements = new String[]{"insert into useraccounts(id,username,password) values (null,'test','test')",
                // settings tabel:
                "ALTER TABLE " + SettingsRepository.TABLE_NAME + " ADD COLUMN USER_ID INT (6)",
                "UPDATE " + SettingsRepository.TABLE_NAME + " SET USER_ID = (select id from useraccounts where username='test')",
                "ALTER TABLE " + SettingsRepository.TABLE_NAME + " MODIFY COLUMN USER_ID INT (6) NOT NULL",
                "ALTER TABLE " + SettingsRepository.TABLE_NAME + " ADD FOREIGN KEY (USER_ID) REFERENCES " + UserAcccountRepository.TABLE_NAME + "(id)",
                // Food tabel:
                "ALTER TABLE " + FoodRepository.TABLE_NAME + " ADD COLUMN USER_ID INT (6)",
                "UPDATE " + FoodRepository.TABLE_NAME + " SET USER_ID = (select id from useraccounts where username='test')",
                "ALTER TABLE " + FoodRepository.TABLE_NAME + " MODIFY COLUMN USER_ID INT (6) NOT NULL",
                "ALTER TABLE " + FoodRepository.TABLE_NAME + " ADD FOREIGN KEY (USER_ID) REFERENCES " + UserAcccountRepository.TABLE_NAME + "(id)",
                // logentry tabel:
                "ALTER TABLE " + LogEntryRepository.TABLE_NAME + " ADD COLUMN USER_ID INT (6)",
                "UPDATE " + LogEntryRepository.TABLE_NAME + " SET USER_ID = (select id from useraccounts where username='test')",
                "ALTER TABLE " + LogEntryRepository.TABLE_NAME + " MODIFY COLUMN USER_ID INT (6) NOT NULL",
                "ALTER TABLE " + LogEntryRepository.TABLE_NAME + " ADD FOREIGN KEY (USER_ID) REFERENCES " + UserAcccountRepository.TABLE_NAME + "(id)",
                // meal tabel:
                "ALTER TABLE " + MealRepository.TABLE_NAME + " ADD COLUMN USER_ID INT (6)",
                "UPDATE " + MealRepository.TABLE_NAME + " SET USER_ID = (select id from useraccounts where username='test')",
                "ALTER TABLE " + MealRepository.TABLE_NAME + " MODIFY COLUMN USER_ID INT (6) NOT NULL",
                "ALTER TABLE " + MealRepository.TABLE_NAME + " ADD FOREIGN KEY (USER_ID) REFERENCES " + UserAcccountRepository.TABLE_NAME + "(id)"
        };

        runStatements(statements);
    }

    public static void updateUserAccountsWithEmail() {
        String sql = "ALTER TABLE " + UserAcccountRepository.TABLE_NAME +
                " ADD COLUMN email TEXT NOT NULL";
        try (Connection connection = DatabaseHelper.getInstance().getConnection();
             CallableStatement currStatement = connection.prepareCall(sql)) {
            LOGGER.info(sql);
            currStatement.execute();
        } catch (SQLException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public static void updateSettings(){
        String[] statements = new String[]{"alter table settings modify column setting text(50)",
                "ALTER TABLE settings ADD UNIQUE user_set(user_id, setting(50))"};
        runStatements(statements);
    }

    private static void runStatements(String[] statements) {
        LOGGER.debug("Running update scripts:");
        for (String statement : statements) {
            try (Connection connection = DatabaseHelper.getInstance().getConnection();
                 CallableStatement currStatement = connection.prepareCall(statement)) {
                LOGGER.info(statement);
                currStatement.execute();

            } catch (SQLException e) {
                LOGGER.error(e.getMessage());
            }
        }
        LOGGER.debug("Finished running update scripts.");
    }

}
