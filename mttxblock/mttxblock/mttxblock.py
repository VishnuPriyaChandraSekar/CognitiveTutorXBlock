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

    typechecker = String(
        default="", scope=Scope.content,
        help="TypeChecker class name"
    )

    display_name = String(
        default="CTAT tutor", scope=Scope.content
    )
    studentID = Integer(default=0, scope=Scope.user_state)

    xblockID = String(default="", scope=Scope.user_state)

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
        self.studentID = self.runtime.user_id
        self.xblockID = str(unicode(self.scope_ids.usage_id))
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
            self.tutorname = data['tutorname']
            self.username = data['username']
            self.htmlurl = data['htmlurl']
            self.hintoptions = data['hintoptions']
            self.probselection = data['probselection']
            print self.tutorname
            return {'result': 'success'}
        except Exception as err:
            return {'result': 'fail', 'error': unicode(err)}

    @XBlock.json_handler
    def load_tutor(self, data, context=None):
        try:
            return {'username': self.username, 'tutorname': self.tutorname, 'probselection': self.probselection,
                    'hintoptions': self.hintoptions, 'studentID': self.studentID}
        except Exception as err:
            return {'result': 'fail', 'error': unicode(err)}

    @XBlock.json_handler
    def log_data(self, data, context=None):
        school_details = self.xblockID.split('+')
        school = school_details[0].split(':')[1]
        class_name = school_details[1]
        course = str(self.scope_ids.usage_id.course_key)
        ts = time.time()
        timestamp = datetime.datetime.fromtimestamp(ts).strftime('%Y-%m-%d %H:%M:%S')
        last_submission_time = self.logging.util_create_custom_field('last_submission_time', timestamp)
        seed = self.logging.util_create_custom_field('seed', '1')
        input_state = self.logging.util_create_input_state('mttxblock_input_state')

        if data['selection'] == 'done':
            done = self.logging.util_create_custom_field('done', 'true')
        else:
            done = self.logging.util_create_custom_field('done', 'false')

        if data['action'] == 'ButtonPressed':
            student_answers = self.logging.util_create_student_answers('mttxblock_answers', data['selection'])
        else:
            student_answers = self.logging.util_create_student_answers('mttxblock_answers', data['input'])

        if data['selection'] == 'hint':
            hintlist = data['hintMessage']
            question_related = self.logging.util_create_question_related(self.display_name, data['question'],
                                                                         str(data['problemNo']), data['question'],
                                                                         'correct_answer_undefined', data['skillName'],
                                                                         'HINT_REQUEST', data['selection'],
                                                                         str(data['attempts']), data['selection'],
                                                                         data['action'], data['input'],
                                                                         hintlist[data['hintLevel']],
                                                                         str(data['hintLevel'] + 1), str(len(hintlist)),
                                                                         school, class_name, course,
                                                                         'section_undefined', 'subsection_undefined',
                                                                         'unit_undefined')
            correct_map = self.logging.util_create_correct_map('mttxblock', ''.join(hintlist), 'true',
                                                               str(data['correctness']), hintlist[data['hintLevel']])
            attempts = '"attempts": "N/A"'
        else:
            question_related = self.logging.util_create_question_related(self.display_name, data['question'],
                                                                         str(data['problemNo']), data['question'],
                                                                         'correct_answer_undefined', data['skillName'],
                                                                         'ATTEMPT', data['selection'],
                                                                         str(data['attempts']), data['selection'],
                                                                         data['action'], data['input'], "N/A", "N/A",
                                                                         "N/A", school, class_name, course,
                                                                         'section_undefined',
                                                                         'subsection_undefined_undefined',
                                                                         'unit_undefined')
            correct_map = self.logging.util_create_correct_map('mttxblock', 'N/A', 'false', str(data['correctness']),
                                                               'N/A')
            attempts = '"attempts": "' + str(data['attempts']) + '"'

        state = self.logging.util_create_state_json(correct_map, input_state, last_submission_time, attempts, seed,
                                                    done, student_answers, question_related)
        print state
        student_module_id = self.logging.util_get_module_id(self.studentID, self.xblockID)
        #DB.util_save_activity(state, timestamp, student_module_id)
        return {'success': 'ok', 'selection': data['selection'], 'input': data['input'], 'hints': data['hintMessage']}

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
