package com.mbhutto.model;

import com.mbhutto.entity.Department;
import com.mbhutto.entity.Result;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import static java.sql.Statement.RETURN_GENERATED_KEYS;

public final class DepartmentDataOperations
{
	private static ResultSet resultSet						 ;
	private static int[] days    = {1, 2, 3, 4, 5}			 ;
	private static int[] classes = {1, 2, 3, 4, 5, 6, 7}     ;

    // ADD DEPARTMENT
	public static long addDepartment(Department department) throws SQLException, ClassNotFoundException
	{
		long departmentId = -1 ;
		String query = "INSERT INTO departments" +
						"(department_name, department_short_name, department_terms, department_sections)" +
						"VALUES" +
						"(?, ?, ?, ?)" ;

		PreparedStatement insertPreparedStatement = Connect.getConnection().prepareStatement(query , RETURN_GENERATED_KEYS);
		insertPreparedStatement.setString(1, department.getDepartmentName().trim())			;
		insertPreparedStatement.setString(2, department.getDepartmentShortName().trim()) 	;
		insertPreparedStatement.setInt(3, department.getDepartmentTerms())            		;
		insertPreparedStatement.setInt(4, department.getDepartmentSections())				;
		int inserted = insertPreparedStatement.executeUpdate()			                    ;

		if(inserted >= 1)
		{
			resultSet = insertPreparedStatement.getGeneratedKeys();
			if (resultSet.next())
			{
				departmentId = resultSet.getLong(1);
			}
			resultSet.close();
		}
		insertPreparedStatement.close();
		Connect.getConnection().setAutoCommit(false);
		String insertQuery = "INSERT INTO departments_terms_sections" +
				"(department_id, term_id, section_id, is_time_table_set)" +
				"VALUES" +
				"(?, ?, ?, ?)" ;

		PreparedStatement preparedStatement = Connect.getConnection().prepareStatement(insertQuery , RETURN_GENERATED_KEYS);
		for (int i=1; i<=department.getDepartmentTerms(); i++)
		{

			for (int j=1; j<=department.getDepartmentSections(); j++)
			{
				preparedStatement.setLong(1, departmentId);
				preparedStatement.setInt(2, i);
				preparedStatement.setInt(3, j);
				preparedStatement.setInt(4, 0);
				preparedStatement.addBatch();
				System.out.println(preparedStatement.toString());
			}
		}

		preparedStatement.executeBatch();
		ResultSet res = preparedStatement.getGeneratedKeys();
		long deptTermSecId = -1 ;
		String insertIntoQuery = "INSERT INTO time_table(departments_terms_sections_id, day, class_no) " +
				"VALUE (?, ?, ?)";
		System.out.println("Auto-incremented values of the column ID generated by the current PreparedStatement object: ");
		PreparedStatement insertTimeTablePreparedStatement = Connect.getConnection().prepareStatement(insertIntoQuery);
		while (res.next())
		{
			deptTermSecId = res.getLong(1);
			for(int i=0; i<days.length; i++)
			{
				for(int j=0; j<classes.length; j++)
				{

					if(i==4 && j>4)
					{
						break;
					}

					insertTimeTablePreparedStatement.setLong(1, deptTermSecId);
					insertTimeTablePreparedStatement.setInt(2, days[i]);
					insertTimeTablePreparedStatement.setInt(3, classes[j]);
					insertTimeTablePreparedStatement.addBatch();
				}
			}


			System.out.println("departments_terms_sections:" + res.getLong(1));
		}
		insertTimeTablePreparedStatement.executeBatch();
		Connect.getConnection().commit();
		Connect.getConnection().setAutoCommit(true);
		return departmentId ;
	}
	
	// UPDATE DEPARTMENT
	public static int updateDepartment(Department department) throws SQLException, ClassNotFoundException
	{
		String query = "UPDATE departments " +
						"SET " +
						"department_name = ?, " +
						"department_short_name = ?, " +
						"department_terms = ?, " +
						"department_sections = ? " +
						"WHERE id = ?" ;
		PreparedStatement updatePreparedStatement = Connect.getConnection().prepareStatement(query);
		updatePreparedStatement.setString(1,department.getDepartmentName().trim())				            ;
		updatePreparedStatement.setString(2,department.getDepartmentShortName().trim())		                ;
		updatePreparedStatement.setInt(3,department.getDepartmentTerms())					            	;
		updatePreparedStatement.setInt(4,department.getDepartmentSections())				            	;
		updatePreparedStatement.setLong(5,department.getDepartmentID())						                ;
							
		int rows = updatePreparedStatement.executeUpdate() 													;
		updatePreparedStatement.close()																		;
		return (rows)																						;
	}

	// DELETE DEPARTMENT
	public static int deleteDepartment(Department department) throws SQLException, ClassNotFoundException {
		PreparedStatement preparedStatement1 = Connect.getConnection().prepareStatement("Delete from departments where department_name = ?");
		preparedStatement1.setString(1,department.getDepartmentName())		 ;
		int rows = preparedStatement1.executeUpdate()                        ;
		preparedStatement1.close()					                         ;
		return rows                                                          ;
	}

	// VIEW ALL DEPARTMENTS
	public static Result viewDepartments() throws SQLException, ClassNotFoundException
	{
		Result result ;
		String query = "SELECT * FROM departments";
    	PreparedStatement preparedStatement = Connect.getConnection().prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		ResultSet resultSet = preparedStatement.executeQuery();

		String records[][];
		int i = 0;

		
		if (resultSet.last())
		{
			int totalRecords = resultSet.getRow();
			result = new Result(totalRecords);
			
			resultSet.beforeFirst();
			records = new String[totalRecords][5];
			while (resultSet.next())
			{
				records[i][0] = String.valueOf(resultSet.getLong(1))				;
				records[i][1] = resultSet.getString(2)								;
				records[i][2] = resultSet.getString(3)								;
				records[i][3] = String.valueOf(resultSet.getInt(4))					;
				records[i][4] = String.valueOf(resultSet.getInt(5))					;
				i++																    ;
			}
			result.setRecords(records);
		}
		else
		{
			result = new Result(0);
		}

		resultSet.close() 			;
		preparedStatement.close()   ;
		return result	 			;
	}

	// Getting Department Teachers
	public static Result getDeptTeachers(Long id) throws SQLException, ClassNotFoundException
	{
		Result result ;
		String query = "SELECT id, teacher_name, teacher_qualification, teacher_designation " +
				"FROM teachers " +
				"WHERE department_id = ?";
		PreparedStatement preparedStatement = Connect.getConnection().prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		preparedStatement.setLong(1,id);
		ResultSet resultSet = preparedStatement.executeQuery();

		String records[][];
		int i = 0;

		if (resultSet.last())
		{
			int totalRecords = resultSet.getRow();
			result = new Result(totalRecords);

			resultSet.beforeFirst();
			records = new String[totalRecords][4];
			while (resultSet.next())
			{
				records[i][0] = String.valueOf(resultSet.getLong(1));
				records[i][1] = resultSet.getString(2);
				records[i][2] = resultSet.getString(3);
				records[i][3] = resultSet.getString(4);
				i++;
			}
			result.setRecords(records);
		}
		else
		{
			result = new Result(0);
		}

		resultSet.close() 			;
		preparedStatement.close()   ;
		return result	 			;
	}

	// This method is used to get the subjects that are taught in different terms of department
	public static Result getDeptTermSubjects(Long deptId, int termId) throws SQLException, ClassNotFoundException
	{
		Result result ;
		String query = "SELECT subject_id FROM subjects_departments_terms_sections " +
				"WHERE departments_terms_sections_id = " +
				"(SELECT id FROM departments_terms_sections WHERE department_id = ? AND term_id = ? AND section_id = 1)";


		PreparedStatement preparedStatement = Connect.getConnection().prepareStatement(query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
		preparedStatement.setLong(1,deptId);
		preparedStatement.setInt(2,termId);
		System.out.println(preparedStatement.toString());
		ResultSet resultSet = preparedStatement.executeQuery();

		String records[][];
		int i = 0;
		Long subjectId;

		if (resultSet.last())
		{
			int totalRecords = resultSet.getRow();
			result = new Result(totalRecords);
			resultSet.beforeFirst();
			records = new String[totalRecords][5];

			while (resultSet.next())
			{
				subjectId = resultSet.getLong(1);

				String Query  =  "SELECT id, subject_code, subject_name, subject_theory_classes_in_week, subject_practical_classes_in_week " +
						"FROM subjects WHERE id = ?";

				PreparedStatement preparedStatement1 = Connect.getConnection().prepareStatement(Query, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				preparedStatement1.setLong(1,subjectId);
				ResultSet resultSet1 = preparedStatement1.executeQuery();

				while (resultSet1.next())
				{
					records[i][0] = String.valueOf(resultSet1.getLong(1));
					records[i][1] = resultSet1.getString(2);
					records[i][2] = resultSet1.getString(3);
					records[i][3] = String.valueOf(resultSet1.getInt(4));
					records[i][4] = String.valueOf(resultSet1.getInt(5));
					i++;
				}
				result.setRecords(records);

			}
		}

		else
		{
			result = new Result(0);
		}

		resultSet.close() 			;
		preparedStatement.close()   ;
		return result	 			;
	}
	
	// SEARCH DEPARTMENT
	public static String[] searchDepartment(String departmentName) throws SQLException, ClassNotFoundException
	{
		departmentName = "%" + departmentName + "%" ;
		String query = "SELECT * from departments WHERE department_name LIKE ?" ;
		PreparedStatement preparedStatement1 = Connect.getConnection().prepareStatement(query);
		preparedStatement1.setString(1, departmentName);
		ResultSet resultSet = preparedStatement1.executeQuery();
		resultSet.next() ;
		String[] values = new String[5];
		values[0] = String.valueOf(resultSet.getLong(1))	;
		values[1] = resultSet.getString(2)					;
		values[2] = resultSet.getString(3)					;
		values[3] = String.valueOf(resultSet.getInt(4))	;
		values[4] = String.valueOf(resultSet.getInt(5))	;
		resultSet.close() 			;
		preparedStatement1.close()	;
		return values				;
	}

}