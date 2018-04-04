"""TO-DO: Write a description of what this XBlock is."""
import datetime
import time
import pkg_resources
from xblock.core import XBlock
from xblock.fields import Integer, Scope, String
from xblock.fragment import Fragment
from xblock_utils import DB


class ModelTracerXBlock(XBlock):
    """
    TO-DO: document what your XBlock does.
    """

    # Fields are defined on the class.  You can access them in your code as
    # self.<fieldname>.

    # TO-DO: delete count, and define your own fields.
    count = Integer(
        default=0, scope=Scope.user_state,
        help="A simple counter, to show something happening",
    )

    tutorname = String(
        default="", scope=Scope.content,
        help="enter a name for your tutor",
    )

    username = String(
        default="", scope=Scope.content,
        help="username",
    )

    htmlurl = String(
        default="", scope=Scope.content,
        help="enter url of the html file",
    )

    hintoptions = String(
        default="", scope=Scope.content,
        help="enter either enable or disable for hint",
    )

    probselection = String(
        default="", scope=Scope.content,
        help="enter either mostL, leastL, mostmastery or leastmastery",
    )

    typechecker = String(default="",scope=Scope.content,help="Type Checker")
    inputmatcher = String(default="",scope=Scope.content,help="InputMatcher class name")

    display_name = String(default="Cognitive Tutor", scope=Scope.content)
    studentID = Integer(default=0, scope=Scope.user_state)

    xblockID = String(default="", scope=Scope.user_state)

    pastel_id = String(scope=Scope.user_state)

    section = String(default="",scope=Scope.content)

    subsection = String(default="", scope=Scope.content)

    unit = String(default="", scope=Scope.content)

    session_id = String(default="", scope=Scope.user_info)

    page_id = String(default="", scope=Scope.user_state)

    dynamic_link = String(default="", scope=Scope.user_state)

    kc = String(default="", scope=Scope.content)

    seq_number = Integer(default=0, scope=Scope.user_info)

    logging = DB()


    def resource_string(self, path):
        """Handy helper for getting resources from our kit."""
        data = pkg_resources.resource_string(__name__, path)
        return data.decode("utf8")

    # TO-DO: change this view to display your data your own way.
    def student_view(self, context=None):
        """
        The primary view of the ModelTracerXBlock, shown to students
        when viewing courses.
        """
        html = self.resource_string("static/html/mttxblock.html")
        frag = Fragment(html.format(self=self))
        frag.add_css(self.resource_string("static/css/mttxblock.css"))
        frag.add_javascript(self.resource_string("static/js/src/mttxblock.js"))
        frag.initialize_js('ModelTracerXBlock')
        return frag

    def studio_view(self, context=None):
        html = self.resource_string("static/html/mttxblockstudio.html")
        fragment = Fragment(html.format(self=self))
        fragment.add_javascript(self.resource_string("static/js/src/mttxblockstudio.js"))
        fragment.initialize_js('ModelTracerXBlockStudio')
        return fragment

    @XBlock.json_handler
    def studio_save(self, data, suffix=''):
        try:
            self.display_name = data['display_name']
            self.tutorname = data['tutorname']
            self.kc = data['skillname']
            self.username = data['username']
            self.htmlurl = data['htmlurl']
            self.hintoptions = data['hintoptions']
            self.probselection = data['probselection']
            self.typechecker = data['username'] + '.' + data['tutorname'] + '.' + data['typechecker']
            if len(data['inputmatcher']) > 0:
                self.inputmatcher = '-ssInputMatcher '+ data['username'] + '.' + data['tutorname'] + '.' + data['inputmatcher']
                print 'Input Matcher ' + self.inputmatcher
            self.section = data['section']
            self.subsection = data['subsection']
            self.unit = data['unit']
            return {'result': 'success'}
        except Exception as err:
            return {'result': 'fail', 'error': unicode(err)}

    @XBlock.json_handler
    def module_skill_name_saved(self, data, suffix=''):
        url = str(data.get('location_id'))
        xblockID = str(unicode(self.scope_ids.usage_id))
        paragraph_id = str(self.scope_ids.usage_id.block_id).replace("course","block")
        course_id = str(self.scope_ids.usage_id.course_key)
        location_id = course_id + "$" + url + "$" + paragraph_id
        self.logging.util_save_module_skillname(self.kc,xblockID, location_id)
        return {'result': 'success'}

    @XBlock.json_handler
    def load_tutor(self, data, context=None):
        print 'called here'
        try:
            self.page_id = data['pageid']
            self.studentID = self.runtime.user_id
            self.xblockID = str(unicode(self.scope_ids.usage_id))
            self.pastel_id = self.logging.util_get_pastel_student_id(self.studentID, str(self.scope_ids.usage_id.course_key))
            if self.pastel_id is None:
                self.pastel_id = "admin"
            self.dynamic_link = self.logging.util_get_dynamic_enable(str(self.studentID),str(self.scope_ids.usage_id.course_key))
            if self.pastel_id is not None:
                print 'pastel ID ###### ' + self.pastel_id + ' page ID ' + self.page_id
            return {'username': self.username, 'tutorname': self.tutorname, 'probselection': self.probselection,
                    'hintoptions': self.hintoptions, 'studentID': self.studentID, 'typechecker': self.typechecker,'inputmatcher':self.inputmatcher, 'dynamiclink':self.dynamic_link, 'kc': self.kc}
        except Exception as err:
            return {'result': 'fail', 'error': unicode(err)}

    @XBlock.json_handler
    def refresh_session(self, data, suffix=''):
        self.session_id = self.logging.util_generate_session_id(str(self.runtime.user_id))
        self.seq_number = 0;

    @XBlock.json_handler
    def get_skill_mapping(self, data, suffix=''):
        print 'skill mapping called here'
        xblock_id = str(unicode(self.scope_ids.usage_id))
        print " KC " + self.kc + " XBlock " + xblock_id
        url = self.logging.util_get_skill_mapping(xblock_id, self.kc)
        print url
        return {'course_id': url[0], "location_id": url[1], "paragraph_id": url[2]}

    @XBlock.json_handler
    def log_data(self, data, context=None):
        print data

        self.seq_number += 1
        if self.session_id == '' or self.session_id is None:
            self.session_id = self.logging.util_generate_session_id(str(self.runtime.user_id))
        if data['selection'] == 'N/A':
            step_name = 'N/A'
        else:
            step_name = data['selection']+' '+str(data['input'])

        hintList = data['feedback']

        if isinstance(hintList, list):
            help_level = data['help_level']
            print str(help_level)
            feedback_text = hintList[data['help_level']]
            total_hints = str(len(hintList))
        else:
            feedback_text = 'N/A'
            total_hints = ''

        if self.pastel_id == "admin":
            self.pastel_id = "admin"
            school = 'admin'
            class_name = 'admin'
            condition = 'admin'
        else:
            course_id = str(self.scope_ids.usage_id.course_key)
            print 'course_id '+' '+course_id
            school, class_name = self.logging.util_find_school_class_bypastelid(self.pastel_id)
            condition = self.logging.util_find_condition_by_course(course_id, self.pastel_id)

        cf = data['cf_field']
        cf['cf_course'] = str(self.scope_ids.usage_id.course_key)
        cf['cf_section'] = self.section
        cf['cf_subsection'] = self.subsection
        cf['cf_unit'] = self.unit
        cf['cf_unit_id'] = str(self.scope_ids.usage_id.block_id).replace("course", "block")
        cf['cf_user_runtime_id'] = self.studentID
        cf['cf_student_pastel_id'] = self.pastel_id
        cf['cf_question'] = data['problem_name']
        cf['cf_choices'] = 'N/A'
        cf['cf_video_url'] = 'N/A'
        cf['cf_video_position'] = 'N/A'
        cf['cf_page_id'] = self.page_id
        cf['cf_seq_number'] = self.seq_number

        response_details = self.logging.util_create_response_details(str(data['timestamp']), self.session_id, data['timezone'], data['student_response_type'],'N/A','RESULT','N/A','N/A',str(data['problem_name']), str(data['problem_view']))
        problem_details = self.logging.util_create_problem_details(step_name,str(data['attempts']),data['outcome'],data['selection'], data['action'],str(data['input']),feedback_text,'N/A',str(data['help_level']),total_hints)
        application_details = self.logging.util_create_application_details(condition,'N/A',data['kc'],'CogTutor',school,class_name)
        cf_details = self.logging.util_create_cf_details(cf)
        json = self.logging.util_create_json(response_details+problem_details+application_details+cf_details)
        student_module_id = 0
        print json

        self.logging.util_save_user_activity(json, data['timestamp'], student_module_id)
        return {'success': 'ok', 'selection': data['selection'], 'input': data['input'], 'hints': feedback_text}

    # TO-DO: change this to create the scenarios you'd like to see in the
    # workbench while developing your XBlock.
    @staticmethod
    def workbench_scenarios():
        """A canned scenario for display in the workbench."""
        return [
            ("ModelTracerXBlock",
             """<mttxblock/>
             """),
            ("Multiple ModelTracerXBlock",
             """<vertical_demo>
                <mttxblock/>
                <mttxblock/>
                <mttxblock/>
                </vertical_demo>
             """),
        ]
