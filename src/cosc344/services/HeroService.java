package cosc344.services;

import cosc344.models.Hero;
import cosc344.utils.NotFoundException;
import java.sql.*;
import java.util.ArrayList;

public class HeroService {
    private Connection conn;

    public HeroService(Connection connection) throws SQLException  {
        this.conn = connection;
    }

    /**
     * getObject-method. This will create and load valueObject contents from database
     * using given Primary-Key as identifier. This method is just a convenience method
     * for the real load-method which accepts the valueObject as a parameter. Returned
     * valueObject will be created using the createValueObject() method.
     */
    public Hero getObject(int id) throws NotFoundException, SQLException {

        Hero valueObject = new Hero();
        valueObject.setId(id);
        load(valueObject);
        return valueObject;
    }


    /**
     * load-method. This will load valueObject contents from database using
     * Primary-Key as identifier. Upper layer should use this so that valueObject
     * instance is created and only primary-key should be specified. Then call
     * this method to complete other persistent information. This method will
     * overwrite all other fields except primary-key and possible runtime variables.
     * If load can not find matching row, NotFoundException will be thrown.
     *
     * @param valueObject  This parameter contains the class instance to be loaded.
     *                     Primary-key field must be set for this to work properly.
     */
    public void load(Hero valueObject) throws NotFoundException, SQLException {

        String sql = "SELECT hero.pid,hero.hname,hero.bdate,hero.exp,HLEVEL.hlevel FROM hero JOIN HLEVEL ON hero.exp = HLEVEL.hexp WHERE (pid = ? ) ";
        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, valueObject.getId());

            singleQuery(stmt, valueObject);

        } finally {
            if (stmt != null)
                stmt.close();
        }
    }


    /**
     * LoadAll-method. This will read all contents from database table and
     * build a List containing valueObjects. Please note, that this method
     * will consume huge amounts of resources if table has lot's of rows.
     * This should only be used when target tables have only small amounts
     * of data.
     *
     */
    public ArrayList<Hero> loadAll() throws SQLException {
        String sql = "SELECT hero.pid,hero.hname,hero.bdate,hero.exp,HLEVEL.hlevel FROM hero JOIN HLEVEL ON hero.exp = HLEVEL.hexp ORDER BY pid ASC ";
        ArrayList<Hero> searchResults = listQuery(conn.prepareStatement(sql));
        return searchResults;
    }



    /**
     * create-method. This will create new row in database according to supplied
     * valueObject contents. Make sure that values for all NOT NULL columns are
     * correctly specified. Also, if this table does not use automatic surrogate-keys
     * the primary-key must be specified. After INSERT command this method will
     * read the generated primary-key back to valueObject if automatic surrogate-keys
     * were used.
     *
     * @param valueObject  This parameter contains the class instance to be created.
     *                     If automatic surrogate-keys are not used the Primary-key
     *                     field must be set for this to work properly.
     */
    public synchronized void create(Hero valueObject) throws SQLException {

        String sql = "";
        PreparedStatement stmt = null;
        ResultSet result = null;

        try {
            sql = "INSERT INTO hero ( pid, hname, bdate, "
                    + "exp) VALUES (?, ?, ?, ?) ";
            stmt = conn.prepareStatement(sql);

            stmt.setInt(1, valueObject.getId());
            stmt.setString(2, valueObject.getName());
            stmt.setDate(3, valueObject.getBirthday());
            stmt.setInt(4, valueObject.getExp());

            int rowcount = databaseUpdate(stmt);
            if (rowcount != 1) {
                System.out.println("PrimaryKey Error when updating DB!");
                throw new SQLException("PrimaryKey Error when updating DB!");
            }

        } finally {
            if (stmt != null)
                stmt.close();
        }


    }


    /**
     * save-method. This method will save the current state of valueObject to database.
     * Save can not be used to create new instances in database, so upper layer must
     * make sure that the primary-key is correctly specified. Primary-key will indicate
     * which instance is going to be updated in database. If save can not find matching
     * row, NotFoundException will be thrown.
     *
     * @param valueObject  This parameter contains the class instance to be saved.
     *                     Primary-key field must be set for this to work properly.
     */
    public void save(Hero valueObject)
            throws NotFoundException, SQLException {

        String sql = "UPDATE hero SET hname = ?, bdate = ?, exp = ?, "
                + "WHERE (pid = ? ) ";
        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, valueObject.getName());
            stmt.setDate(2, valueObject.getBirthday());
            stmt.setInt(3, valueObject.getExp());

            stmt.setInt(4, valueObject.getId());

            int rowcount = databaseUpdate(stmt);
            if (rowcount == 0) {
                System.out.println("Object could not be saved! (PrimaryKey not found)");
                throw new NotFoundException("Object could not be saved! (PrimaryKey not found)");
            }
            if (rowcount > 1) {
                System.out.println("PrimaryKey Error when updating DB! (Many objects were affected!)");
                throw new SQLException("PrimaryKey Error when updating DB! (Many objects were affected!)");
            }
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }


    /**
     * delete-method. This method will remove the information from database as identified by
     * by primary-key in supplied valueObject. Once valueObject has been deleted it can not
     * be restored by calling save. Restoring can only be done using create method but if
     * database is using automatic surrogate-keys, the resulting object will have different
     * primary-key than what it was in the deleted object. If delete can not find matching row,
     * NotFoundException will be thrown.
     *
     * @param valueObject  This parameter contains the class instance to be deleted.
     *                     Primary-key field must be set for this to work properly.
     */
    public void delete(Hero valueObject)
            throws NotFoundException, SQLException {

        String sql = "DELETE FROM hero WHERE (pid = ? ) ";
        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, valueObject.getId());

            int rowcount = databaseUpdate(stmt);
            if (rowcount == 0) {
                System.out.println("Object could not be deleted (PrimaryKey not found)");
                throw new NotFoundException("Object could not be deleted! (PrimaryKey not found)");
            }
            if (rowcount > 1) {
                System.out.println("PrimaryKey Error when updating DB! (Many objects were deleted!)");
                throw new SQLException("PrimaryKey Error when updating DB! (Many objects were deleted!)");
            }
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }


    /**
     * deleteAll-method. This method will remove all information from the table that matches
     * this Dao and ValueObject couple. This should be the most efficient way to clear table.
     * Once deleteAll has been called, no valueObject that has been created before can be
     * restored by calling save. Restoring can only be done using create method but if database
     * is using automatic surrogate-keys, the resulting object will have different primary-key
     * than what it was in the deleted object. (Note, the implementation of this method should
     * be different with different DB backends.)
     *
     */
    public void deleteAll() throws SQLException {

        String sql = "DELETE FROM hero";
        PreparedStatement stmt = null;

        try {
            stmt = conn.prepareStatement(sql);
            int rowcount = databaseUpdate(stmt);
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }


    /**
     * coutAll-method. This method will return the number of all rows from table that matches
     * this Dao. The implementation will simply execute "select count(primarykey) from table".
     * If table is empty, the return value is 0. This method should be used before calling
     * loadAll, to make sure table has not too many rows.
     *
     */
    public int countAll() throws SQLException {

        String sql = "SELECT count(*) FROM hero";
        PreparedStatement stmt = null;
        ResultSet result = null;
        int allRows = 0;

        try {
            stmt = conn.prepareStatement(sql);
            result = stmt.executeQuery();

            if (result.next())
                allRows = result.getInt(1);
        } finally {
            if (result != null)
                result.close();
            if (stmt != null)
                stmt.close();
        }
        return allRows;
    }


    /**
     * searchMatching-Method. This method provides searching capability to
     * get matching valueObjects from database. It works by searching all
     * objects that match permanent instance variables of given object.
     * Upper layer should use this by setting some parameters in valueObject
     * and then  call searchMatching. The result will be 0-N objects in a List,
     * all matching those criteria you specified. Those instance-variables that
     * have NULL values are excluded in search-criteria.
     *
     * @param valueObject  This parameter contains the class instance where search will be based.
     *                     Primary-key field should not be set.
     */
    public ArrayList<Hero> searchMatching(Hero valueObject) throws SQLException {

        ArrayList<Hero> searchResults;

        boolean first = true;
        StringBuffer sql = new StringBuffer("SELECT * FROM hero WHERE 1=1 ");

        if (valueObject.getId() != 0) {
            if (first) { first = false; }
            sql.append("AND pid = ").append(valueObject.getId()).append(" ");
        }

        if (valueObject.getName() != null) {
            if (first) { first = false; }
            sql.append("AND hname LIKE '").append(valueObject.getName()).append("%' ");
        }

        if (valueObject.getBirthday() != null) {
            if (first) { first = false; }
            sql.append("AND bdate = '").append(valueObject.getBirthday()).append("' ");
        }

        if (valueObject.getExp() != 0) {
            if (first) { first = false; }
            sql.append("AND exp = ").append(valueObject.getExp()).append(" ");
        }

        sql.append("ORDER BY pid ASC ");

        // Prevent accidential full table results.
        // Use loadAll if all rows must be returned.
        if (first)
            searchResults = new ArrayList<Hero>();
        else
            searchResults = listQuery(conn.prepareStatement(sql.toString()));

        return searchResults;
    }


    /**
     * databaseUpdate-method. This method is a helper method for internal use. It will execute
     * all database handling that will change the information in tables. SELECT queries will
     * not be executed here however. The return value indicates how many rows were affected.
     * This method will also make sure that if cache is used, it will reset when data changes.
     *
     * @param stmt         This parameter contains the SQL statement to be excuted.
     */
    protected int databaseUpdate(PreparedStatement stmt) throws SQLException {

        int result = stmt.executeUpdate();

        return result;
    }



    /**
     * databaseQuery-method. This method is a helper method for internal use. It will execute
     * all database queries that will return only one row. The resultset will be converted
     * to valueObject. If no rows were found, NotFoundException will be thrown.
     *
     * @param stmt         This parameter contains the SQL statement to be excuted.
     * @param valueObject  Class-instance where resulting data will be stored.
     */
    protected void singleQuery(PreparedStatement stmt, Hero valueObject)
            throws NotFoundException, SQLException {

        ResultSet result = null;

        try {
            result = stmt.executeQuery();

            if (result.next()) {

                valueObject.setId(result.getInt("pid"));
                valueObject.setName(result.getString("hname"));
                valueObject.setBirthday(result.getDate("bdate"));
                valueObject.setExp(result.getInt("exp"));
                valueObject.setLevel(result.getInt("hlevel"));

            } else {
                System.out.println("Hero Object Not Found!");
                throw new NotFoundException("Hero Object Not Found!");
            }
        } finally {
            if (result != null)
                result.close();
            if (stmt != null)
                stmt.close();
        }
    }


    /**
     * databaseQuery-method. This method is a helper method for internal use. It will execute
     * all database queries that will return multiple rows. The resultset will be converted
     * to the List of valueObjects. If no rows were found, an empty List will be returned.
     *
     * @param stmt         This parameter contains the SQL statement to be excuted.
     */
    protected ArrayList<Hero> listQuery(PreparedStatement stmt) throws SQLException {

        ArrayList<Hero> searchResults = new ArrayList<>();
        ResultSet result = null;

        try {
            result = stmt.executeQuery();

            while (result.next()) {
                Hero temp = new Hero();

                temp.setId(result.getInt("pid"));
                temp.setName(result.getString("hname"));
                temp.setBirthday(result.getDate("bdate"));
                temp.setExp(result.getInt("exp"));
                temp.setLevel(result.getInt("hlevel"));

                searchResults.add(temp);
                System.out.println(temp.toString());
            }

        } finally {
            if (result != null)
                result.close();
            if (stmt != null)
                stmt.close();
        }

        return searchResults;
    }

}