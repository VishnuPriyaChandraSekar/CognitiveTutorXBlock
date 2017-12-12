import MySQLdb
import datetime
import time


# from DBUtils.PooledDB import PooledDB


class DB:

    def print_function(par):
        print "Hello : ", par
        return

    def util_create_state_json(self, correct_map, input_state, last_submission_time, attempts, seed, done,
                               student_answers,
                               question_related):
        state = '{' + correct_map + ',' + input_state + ',' + last_submission_time + ',' + attempts + ',' + seed + ',' + done + ',' + student_answers + ',' + question_related + '}'
        return state

    def util_create_correct_map(self, custom_field, hint, hint_mode, correctness, hint_message):
        correct_map = '"correct_map":{"' + custom_field + '":{"hint":"' + hint + '", "hintmode": "' + hint_mode + '", "correctness":"' + correctness + '", "msg": "' + hint_message + '", "answervarible": null, "npoints": null, "queuestate": null}}'
        return correct_map

    def util_create_question_related(self, display_name, problem_name, problem_id, question, correct_answer, skill_name,
                                     student_response_type, step_name, attempts, selection, action, student_input, feedback_text,
                                     help_level, total_hints, school, class_name, course, section, subsection, unit):
        print "building the question relation"
        question_related = '"question_details":{"display_name":"' + display_name + '", "problem name":"' + problem_name + '", "problemId": "' + problem_id + '", "question": "' + question + '", "correct_answer": "' + correct_answer + '", "user_answer":"' + student_input + '","skillname":"' + skill_name + '", "kc": "' + skill_name + '", "time zone": "' + str(time.tzname[time.daylight]) + '", "student response type": "' + student_response_type + '", "student response subtype": "N/A", "tutor response type": "RESULT","tutor response subtype": "N/A", "level": "N/A", "problem view": "1", "step name": "'+selection+'", "attempt at step": "' + attempts + '", "selection": "' + selection + '", "Action": "' + action + '", "input": "' + student_input + '", "feedback text": "' + feedback_text + '", "feedback classification": "N/A", "help level": "' + help_level + '", "total number hints": "' + total_hints + '", "condition name": "N/A", "condition type": "N/A", "kc category": "N/A", "school": "' + school + '", "class": "' + class_name + '", "cf": "N/A", "course": "' + course + '", "section": "' + section + '", "subsection": "' + subsection + '", "unit": "' + unit + '"}'
        print question_related
        return question_related

    def util_create_student_answers(self, custom_field, student_answer):
        student_answers = '"student_answers":{"' + custom_field + '":"' + student_answer + '"}'
        return student_answers

    def util_create_input_state(self, custom_field):
        input_state = '"' + custom_field + '": {}'
        return input_state

    def util_create_custom_field(self, custom_field, value):
        field = '"' + custom_field + '": ' + value
        field2 = '"' + custom_field + '" : "' + value + '"'
        return field

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
