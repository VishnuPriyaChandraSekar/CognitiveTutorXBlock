import MySQLdb
import datetime
import time
import uuid


# from DBUtils.PooledDB import PooledDB




class DB:
    comma = ', '
    colon = ':'

    def print_function(par):
        print "Hello : ", par
        return

    def util_create_response_details(self, timestamp, session_id, time_zone, student_response_type, student_response_subtype, tutor_response_type,
                            tutor_response_subtype, level, problem_name, problem_view):
        field = '"timestamp": "' + timestamp + '", ' + '"session id": "'+session_id+'", '+'"time zone": "' + time_zone + '", ' + '"student response type": "' + student_response_type + '", ' + '"student response subtype": "' + student_response_subtype + '", ' + '"tutor response type": "' + tutor_response_type + '", ' + '"tutor response subtype": "' + tutor_response_subtype + '", ' + '"level": "' + level + '", ' + '"problem name": "' + problem_name + '", ' + '"problem view": "' + problem_view + '", '
        return field

    def util_create_problem_details(self,step_name, attempt_at_step, outcome, selection, action, student_input, feedback_text, feedback_classification, help_level, total_number_hints):
        field = '"step name": "' + step_name + '", ' + '"attempt at step": "' + attempt_at_step + '", ' + '"outcome": "' + outcome + '", ' + '"selection": "' + selection + '", ' + '"action": "' + action + '", ' + '"input": "' + student_input + '", ' + '"feedback text": "' + feedback_text + '", ' + '"feedback classification": "' + feedback_classification + '", ' + '"help level": "' + help_level + '", ' + '"total number hints": "' + total_number_hints + '", '
        return field

    def util_create_application_details(self, condition_name, condition_type, kc, kc_category, school, class_name):
        field = '"condition name": "'+condition_name+'", '+'"condition type": "'+condition_type+'", '+'"kc": "'+kc+'", '+'"kc category": "'+kc_category+'", '+'"school": "'+school+'", '+'"class": "'+class_name+'"'
        return field

    def util_create_cf_details(self,list_of_cf_fields):
        field = ''
        print "cf fields ######"
        print str(list_of_cf_fields)
        for key in list_of_cf_fields:
            if isinstance(list_of_cf_fields[key],int):
                field = field + ', "' + key + '": "' + str(list_of_cf_fields[key])+'"'
            else:
                field = field + ', "' + key + '": "' + list_of_cf_fields[key]+'"'

        return field

    def util_create_json(self, datalog):
        json = '{"question_details":{ '+datalog+'}}'
        return json

    def util_get_module_id(self, student_id, xblock_id):
        """
          This method help us to find module_id for our XBlock
          """

        try:
            db = MySQLdb.connect("127.0.0.1", "root", "", "edxapp")

            cursor = db.cursor()
            sql = """select * from edxapp.courseware_studentmodule where student_id= %s and module_id= %s """
            cursor.execute(sql, (str(student_id), str(xblock_id)))
            result = cursor.fetchone()
            module_id = int(result[0])
            return module_id

        except:
            import traceback
            traceback.print_exc()
            db.rollback()
            print "Database has been rollback!!!"
        finally:
            cursor.close()
            db.close()

    def util_save_user_activity(self, state, timestamp, student_module_id):
        # Mysql database access here:
        try:
            db = MySQLdb.connect("127.0.0.1", "root", "", "edxapp_csmh", charset='utf8')
            cursor = db.cursor()

            sql = """INSERT INTO edxapp_csmh.coursewarehistoryextended_studentmodulehistoryextended(state, created, student_module_id)
                         VALUES (%s, %s, %s)"""

            cursor.execute(sql, (state, timestamp, str(student_module_id)))
            db.commit()
            print "Database finished executing mcqs..."
        except Exception as e:
            print e
            db.rollback()
            print "database rollback!"
        finally:
            db.close()

    def util_save_module_skillname(self, skillname, xblock_id, location_id):
        db = MySQLdb.connect("127.0.0.1", "root", "", "edxapp_csmh", charset='utf8');
        cursor = db.cursor();
        sql = """SELECT * From edxapp_csmh.module_skillname WHERE xblock_id = '%s'"""
        sql1 = """INSERT INTO edxapp_csmh.module_skillname(xblock_id, type, skillname, location) VALUES (%s, %s, %s, %s)"""
        sql2 = """UPDATE edxapp_csmh.module_skillname SET type = %s, skillname = %s, location = %s WHERE xblock_id = %s"""

        try:
            cursor.execute(sql % (xblock_id))
            result = cursor.fetchone()
            if not cursor.rowcount:
                cursor.execute(sql1, (xblock_id, "mcqs", skillname, location_id))
                db.commit()
                print "Skillname has been saved in module_skillname table."
            else:
                cursor.execute(sql2, ("text", skillname, location_id, xblock_id))
        except Exception as e:
            print e
            db.rollback()
            print "Database rollback!"

    def util_get_skill_mapping(self, xblock_id, skillname):
        # start saving it in the database, target table: skill_mapping
        db = MySQLdb.connect("127.0.0.1", "root", "", "edxapp_csmh", charset='utf8')
        cursor = db.cursor()
        sql3 = """select location from edxapp_csmh.module_skillname where id = (select max(id) from edxapp_csmh.module_skillname where type in ("text", "video") and skillname REGEXP %s and id< (select id from edxapp_csmh.module_skillname where xblock_id=%s));"""

        try:
            rskill = ',' + skillname + ',|,' + skillname + '|^' + skillname + '$|^' + skillname + ','
            cursor.execute(sql3, (rskill, xblock_id))
            result = cursor.fetchone()
            if not cursor.rowcount:
                print "No results found"
            else:
                url = result[0].split("$")
                print url
                return url
        except Exception as e:
            print e
            db.rollback()
            print "this skillname didn't have any paragraph matched."
            return {"exception": "this skillname didn't have any paragraph matched."}
        finally:
            db.close()

    def util_update_xblock_for_exporter(self, xblock_id, course_id, section, subsection, unit, type_of_xblock, title,
                                        question,
                                        choices, image_url, correct_answer, hint, problem_name, skillname):
        # start to save the XBlock related information in the database:
        db = MySQLdb.connect("127.0.0.1", "root", "", "edxapp_csmh", charset='utf8')
        cursor = db.cursor();
        # for select the unique xblock_id
        sql0 = """SELECT * FROM edxapp_csmh.export_course_content_and_skill_validation WHERE xblock_id = '%s'"""
        # for insert the xblock information
        sql = """INSERT INTO edxapp_csmh.export_course_content_and_skill_validation(course_id, xblock_id, section, subsection, unit, type_of_xblock, title, question, choices, image_url, correct_answer, hint, problem_name, skillname) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)"""
        # for update the xblock information
        sql1 = """UPDATE edxapp_csmh.export_course_content_and_skill_validation SET course_id = %s, section = %s, subsection = %s, unit = %s, type_of_xblock = %s, title = %s, question = %s, choices = %s, image_url = %s, correct_answer = %s, hint = %s, problem_name = %s, skillname = %s where xblock_id = %s"""
        # for select the same course to see if there is any other xblock type have the same id
        sql2 = """SELECT * FROM edxapp_csmh.export_course_content_and_skill_validation WHERE type_of_xblock = "TextParagraph" AND course_id = %s AND skillname = %s"""

        try:
            cursor.execute(sql0 % xblock_id)
            result = cursor.fetchone()
            if not cursor.rowcount:
                print "No any results(assessment) found, insert a new entry to assessment for multiple choice xblock:"
                try:
                    cursor.execute(sql, (
                        course_id, xblock_id, section, subsection, unit, type_of_xblock, title, question, choices,
                        image_url,
                        correct_answer, hint, problem_name, skillname))
                    db.commit()
                    cursor.execute(sql2, (course_id, skillname))
                    result1 = cursor.fetchone()
                    if not cursor.rowcount:
                        print "No any other xblocks have the same skillname"
                        setBorderColor = 1
                    else:
                        print "Found xblock with the same skillname."
                        setBorderColor = 0
                except Exception as e:
                    print "I am getting error when inserting - assessment."
                    print e
                    db.rollback()
            else:
                print "Found the related entry in database, update the entry in assessment for this xblock."
                try:
                    cursor.execute(sql1, (
                        course_id, section, subsection, unit, type_of_xblock, title, question, choices, image_url,
                        correct_answer, hint, problem_name, skillname, xblock_id))
                    db.commit()
                    cursor.execute(sql2, (course_id, skillname))
                    result1 = cursor.fetchone()
                    if not cursor.rowcount:
                        print "No any other xblocks have the same skillname"
                        setBorderColor = 1
                    else:
                        print "Found xblock with the same skillname."
                        setBorderColor = 0
                except Exception as e:
                    print "I am getting error when updating - assessment."
                    print e
                    db.rollback()
        except Exception as e:
            print "I am getting error when selecting - assessment."
            print e
            db.rollback()
        finally:
            db.close()
            return setBorderColor

    def util_delete_xblock_for_exporter(self, xblock_id):
        db = MySQLdb.connect("127.0.0.1", "root", "", "edxapp_csmh", charset='utf8');
        cursor = db.cursor();
        sql = """DELETE FROM edxapp_csmh.export_course_content_and_skill_validation WHERE xblock_id = '%s' """
        sql1 = """DELETE FROM edxapp_csmh.module_skillname WHERE xblock_id = '%s' """
        try:
            cursor.execute(sql % (xblock_id))
            cursor.execute(sql1 % (xblock_id))
            db.commit()
        except Exception as e:
            print "Error happened when we tried to delete xblock in database."
            print e
            db.rollback()
        finally:
            db.close()

    def util_get_probability(self, skillname, student_id):
        db = MySQLdb.connect("127.0.0.1", "root", "", "edxapp", charset='utf8');
        cursor = db.cursor();
        sql = """SELECT * FROM edxapp_csmh.temporary_probability where skillname = %s and student_pastel_id = %s order by id DESC limit 1;"""

        try:
            cursor.execute(sql, (skillname, student_id))
            result = cursor.fetchone()
            if not cursor.rowcount:
                print "No any results(get_probability) found."

            return result
            db.commit()
        except Exception as e:
            print e
            db.rollback()
        finally:
            db.close()

    def util_get_pastel_student_id(self, user_id):
        db = MySQLdb.connect("127.0.0.1", "root", "", "edxapp", charset='utf8');
        cursor = db.cursor();

        sql = """SELECT * FROM edxapp.auth_user where id = '%s' """

        try:
            cursor.execute(sql % (user_id))
            result = cursor.fetchone()
            if not cursor.rowcount:
                print "No any results(auth_student) found."
                return
            email = str(result[7])
            print "Get username and email from DB: ", str(result[4]) + ", " + str(result[7])

            sql1 = """SELECT * FROM edxapp_csmh.pastel where email = '%s'"""
            if email != "":
                cursor.execute(sql1 % (str(email)))
                result1 = cursor.fetchone()
                if not cursor.rowcount:
                    print "No any results(pastel_student_id) found."
                    return
                print "Get pastel_student_id from DB: ", str(result1[3])
                pastel_student_id = str(result1[3])
                db.commit()
                return pastel_student_id
        except Exception as e:
            print e
            db.rollback()
        finally:
            db.close()

    def util_get_dynamic_enable(self, student_id, course_id):
        print "Get Course id: *" + course_id + "*, user_id is: " + student_id
        db = MySQLdb.connect("127.0.0.1", "root", "", "edxapp", charset='utf8')
        cursor = db.cursor()

        sql = """SELECT * FROM edxapp.auth_user where id = '%s' """
        try:
            cursor.execute(sql % (student_id))
            result = cursor.fetchone()
            if not cursor.rowcount:
                print "No any results(auth_student) found."
                return
            email = str(result[7])
            print "Get username and email from DB: ", str(result[4]) + ", " + str(result[7])

            sql1 = """SELECT * FROM edxapp_csmh.pastel where email = '%s'"""
            sql2 = """SELECT * FROM edxapp_csmh.condition_course_match where condition_name= '%s' and course_id= '%s'"""
            if email != "":
                cursor.execute(sql1 % (str(email)))
                result1 = cursor.fetchone()
                if not cursor.rowcount:
                    print "No any results(pastel_student_id) found."
                    return None, "admin", "admin", "admin", "1", "1"
                condition = str(result1[7])
                print "Get conditon from DB: ", condition

                cursor.execute(sql2 % (condition, course_id))
                result2 = cursor.fetchone()
                enable_dynamic = str(result2[3])

                db.commit()
                cursor.close()
                print 'Dynamic Link : '+enable_dynamic
                return enable_dynamic
        except Exception as e:
            print e
            db.rollback()
        finally:
            cursor.close()
            db.close()

    def util_get_border_color(self, course_id, skillname):
        # start to save the XBlock related information in the database:
        db = MySQLdb.connect("127.0.0.1", "root", "", "edxapp_csmh", charset='utf8');
        cursor = db.cursor();
        # for select the same course to see if there is any other xblock type have the same id
        sql = """SELECT * FROM edxapp_csmh.export_course_content_and_skill_validation WHERE type_of_xblock in ("TextParagraph", "VideoXBlock") AND course_id = %s AND skillname REGEXP %s"""
        try:
            rskill = ',' + skillname + ',|,' + skillname + '|^' + skillname + '$|^' + skillname + ','

            cursor.execute(sql, (course_id, rskill))
            result1 = cursor.fetchone()
            if not cursor.rowcount:
                print "No any other xblocks have the same skillname"
                HtmlSetBorderColor = 0
            else:
                print "Found xblock with the same skillname."
                HtmlSetBorderColor = 1

            return HtmlSetBorderColor
        except Exception as e:
            print "I am getting error when inserting - assessment."
            print e
            db.rollback()
        finally:
            db.close()

    def util_find_condition_by_course(self, course_id):
        db = MySQLdb.connect("127.0.0.1", "root", "", "edxapp_csmh", charset='utf8')
        cursor = db.cursor()

        # we should have only one course match one condition in this table:
        # for select the same course to see if there is any other xblock type have the same id
        sql = "select condition_name from edxapp_csmh.condition_course_match where course_id = '%s' " % (course_id)
        try:
            cursor.execute(sql)
            result = cursor.fetchone()
            print "result length is " + str(len(result))
            if not cursor.rowcount:
                # print "No any condition name found in table condition_course_match."
                return "admin"
            else:
                # print "Found condition name in table condition_course_match: " + result[0]
                return result[0]
        except Exception as e:
            print "I am getting error when finding the condition name from table condition_course_match."
            print e
            db.rollback()
        finally:
            db.close()

    def util_find_school_class_bypastelid(self, pastel_student_id):
        db = MySQLdb.connect("127.0.0.1", "root", "", "edxapp_csmh", charset='utf8');
        cursor = db.cursor();
        # we should have only one course match one condition in this table:
        # for select the same course to see if there is any other xblock type have the same id
        sql = """SELECT school, class from edxapp_csmh.pastel where pastel_student_id = '%s' """
        try:
            cursor.execute(sql % pastel_student_id)
            result1 = cursor.fetchone()
            if not cursor.rowcount:
                # print "No any school name found in table pastel."
                return "admin", "admin"
            else:
                # print "Found school and class name in table pastel."
                return str(result1[0]), str(result1[1])
        except Exception as e:
            print "I am getting error when finding the school and class name from table pastel."
            print e
            db.rollback()
        finally:
            db.close()

    def util_generate_session_id(self, student_id):
        # return uuid.uuid4().hex[:8] + '-' + uuid.uuid4().hex[:4] + '-' + uuid.uuid4().hex[:4] + '-' +
        # uuid.uuid4().hex[:4] + '-' + uuid.uuid4().hex[:12]
        ts = time.time()
        timestamp = datetime.datetime.fromtimestamp(ts).strftime('%Y%m%d%H%M%S')
        return student_id + '-' + uuid.uuid4().hex[:8] + '-' + timestamp