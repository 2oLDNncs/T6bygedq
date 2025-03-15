package t6bygedq.lib.cbl;

import t6bygedq.lib.Helpers;

/**
 * @author 2oLDNncs 20250203
 */
public final class RecData_X0030_Token extends RecData {
	
	public final LongVar            vTokenNumber = this.newLongVar(TOKEN_NUMBER);
	public final EnumVar<TokenCode> vTokenCode   = this.newEnumVar(TOKEN_CODE, TokenCode.decoder);
	public final LongVar            vTokenColumn = this.newLongVar(TOKEN_COLUMN);
	public final LongVar            vTokenLine   = this.newLongVar(TOKEN_LINE);
	public final EnumVar<Flags>     vFlags       = this.newEnumVar(FLAGS, Flags.decoder);
	public final StringVar          vTokenText   = this.newStringVarV(TOKEN_LENGTH);
	
	public RecData_X0030_Token(final Buffer buffer) {
		super(buffer);
	}
	
	private static final Buffer.Region.Generator staticRegionGenerator =
			getStaticRegionGenerator(RecData_X0030_Token.class);
	
	private static final Buffer.Region TOKEN_NUMBER = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region TOKEN_CODE   = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region TOKEN_LENGTH = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region TOKEN_COLUMN = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region TOKEN_LINE   = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region FLAGS        = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region RESERVED_01  = staticRegionGenerator.newFixedLength(7);
	
	static {
		Helpers.ignore(RESERVED_01);
	}
	
	/**
	 * @author 2oLDNncs 20250209
	 */
	public static enum Flags {
		
		NOFLAGS,
		CONTINUED,
		LAST_PIECE,
		;
		
		static final Decoder<Integer, Flags> decoder =
				new ReversibleMap<Integer, Flags>()
				.set(0x00, NOFLAGS)
				.set(0x80, CONTINUED)
				.set(0x40, LAST_PIECE)
				;
	}
	
	/**
	 * @author 2oLDNncs 20250209
	 */
	public static enum TokenCode {
		
		PICTURE_STRING,
		TOKEN_PIECE,
		ACCEPT,
		ADD,
		ALTER,
		CALL,
		CANCEL,
		CLOSE,
		COMPUTE,
		DELETE,
		DISPLAY,
		DIVIDE,
		READY,
		END_PERFORM,
		ENTER,
		ENTRY,
		EXIT,
		EXEC_or_EXECUTE,
		GO,
		IF,
		INITIALIZE,
		INVOKE,
		INSPECT,
		MERGE,
		MOVE,
		MULTIPLY,
		OPEN,
		PERFORM,
		READ,
		RELEASE,
		RETURN,
		REWRITE,
		SEARCH,
		SET,
		SORT,
		START,
		STOP,
		STRING,
		SUBTRACT,
		UNSTRING,
		USE,
		WRITE,
		CONTINUE,
		END_ADD,
		END_CALL,
		END_COMPUTE,
		END_DELETE,
		END_DIVIDE,
		END_EVALUATE,
		END_IF,
		END_MULTIPLY,
		END_READ,
		END_RETURN,
		END_REWRITE,
		END_SEARCH,
		END_START,
		END_STRING,
		END_SUBTRACT,
		END_UNSTRING,
		END_WRITE,
		GOBACK,
		EVALUATE,
		RESET,
		SERVICE,
		END_INVOKE,
		END_EXEC,
		XML,
		END_XML,
		
		//<6.1>
		ALLOCATE,
		FREE,
		JSON,
		END_JSON,
		//</6.1>
		
		FOREIGN_VERB,
		DATA_NAME,
		DASHED_NUM,
		DECIMAL,
		DIV_SIGN,
		EQ,
		EXPONENTIATION,
		GT,
		INTEGER,
		LT,
		LPAREN,
		MINUS_SIGN,
		MULT_SIGN,
		NONUMLIT,
		PERIOD,
		PLUS_SIGN,
		RPAREN,
		SIGNED_INTEGER,
		QUID,
		COLON,
		IEOF,
		EGCS_LIT,
		COMMA_SPACE,
		SEMICOLON_SPACE,
		PROCEDURE_NAME,
		FLT_POINT_LIT,
		LANGUAGE_ENVIRONMENT,
		GE,
		IDREF,
		EXPREF,
		CICS,
		NEW,
		NATIONAL_LIT,
		ADDRESS,
		ADVANCING,
		AFTER,
		ALL,
		ALPHABETIC,
		ALPHANUMERIC,
		ANY,
		AND,
		ALPHANUMERIC_EDITED,
		BEFORE,
		BEGINNING,
		FUNCTION,
		CONTENT,
		CORR_or_CORRESPONDING,
		DAY,
		DATE,
		DEBUG_CONTENTS,
		DEBUG_ITEM,
		DEBUG_LINE,
		DEBUG_NAME,
		DEBUG_SUB_1,
		DEBUG_SUB_2,
		DEBUG_SUB_3,
		DELIMITED,
		DELIMITER,
		DOWN,
		NUMERIC_EDITED,
		XML_EVENT,
		END_OF_PAGE_or_EOP,
		EQUAL,
		ERROR,
		XML_NTEXT,
		EXCEPTION,
		EXTEND,
		FIRST,
		FROM,
		GIVING,
		GREATER,
		I_O,
		IN,
		INITIAL,
		INTO,
		INVALID,
		SQL,
		LESS,
		LINAGE_COUNTER,
		XML_TEXT,
		LOCK,
		GENERATE,
		NEGATIVE,
		NEXT,
		NO,
		NOT,
		NUMERIC,
		KANJI,
		OR,
		OTHER,
		OVERFLOW,
		PAGE,
		CONVERTING,
		POINTER,
		POSITIVE,
		DBCS,
		PROCEDURES,
		PROCEED,
		REFERENCES,
		DAY_OF_WEEK,
		REMAINDER,
		REMOVAL,
		REPLACING,
		REVERSED,
		REWIND,
		ROUNDED,
		RUN,
		SENTENCE,
		STANDARD,
		RETURN_CODE_etc, // Also: SORT-CORE-SIZE, SORT-FILE-SIZE, SORT-MESSAGE, SORT-MODE-SIZE, SORT-RETURN, TALLY, XML-CODE
		TALLYING,
		SUM,
		TEST,
		THAN,
		UNTIL,
		UP,
		UPON,
		VARYING,
		RELOAD,
		TRUE,
		THEN,
		RETURNING,
		ELSE,
		SELF,
		SUPER,
		WHEN_COMPILED,
		ENDING,
		FALSE,
		REFERENCE,
		NATIONAL_EDITED,
		COM_REG,
		ALPHABETIC_LOWER,
		ALPHABETIC_UPPER,
		REDEFINES,
		OCCURS,
		SYNC_or_SYNCHRONIZED,
		MORE_LABELS,
		JUST_or_JUSTIFIED,
		SHIFT_IN,
		BLANK,
		VALUE,
		COMP_or_COMPUTATIONAL,
		COMP_1_or_COMPUTATIONAL_1,
		COMP_3_or_COMPUTATIONAL_3,
		COMP_2_or_COMPUTATIONAL_2,
		COMP_4_or_COMPUTATIONAL_4,
		DISPLAY_1,
		SHIFT_OUT,
		INDEX,
		USAGE,
		SIGN,
		LEADING,
		SEPARATE,
		INDEXED,
		LEFT,
		RIGHT,
		PIC_or_PICTURE,
		VALUES,
		GLOBAL,
		EXTERNAL,
		BINARY,
		PACKED_DECIMAL,
		EGCS,
		PROCEDURE_POINTER,
		COMP_5_or_COMPUTATIONAL_5,
		FUNCTION_POINTER,
		TYPE,
		JNIENVPTR,
		NATIONAL,
		GROUP_USAGE,
		
		//<6.1>
		VOLATILE,
		//</6.1>
		
		HIGH_VALUE_or_HIGH_VALUES,
		LOW_VALUE_or_LOW_VALUES,
		QUOTE_or_QUOTES,
		SPACE_or_SPACES,
		ZERO,
		ZEROES_or_ZEROS,
		NULL_or_NULLS,
		BLOCK,
		BOTTOM,
		CHARACTER,
		CODE,
		CODE_SET,
		FILLER,
		FOOTING,
		LABEL,
		LENGTH,
		LINAGE,
		OMITTED,
		RENAMES,
		TOP,
		TRAILING,
		RECORDING,
		INHERITS,
		RECURSIVE,
		ACCESS,
		ALSO,
		ALTERNATE,
		AREA_or_AREAS,
		ASSIGN,
		COLLATING,
		COMMA,
		CURRENCY,
		CLASS,
		DECIMAL_POINT,
		DUPLICATES,
		DYNAMIC,
		EVERY,
		MEMORY,
		MODE,
		MODULES,
		MULTIPLE,
		NATIVE,
		OFF,
		OPTIONAL,
		ORGANIZATION,
		POSITION,
		PROGRAM,
		RANDOM,
		RELATIVE,
		RERUN,
		RESERVE,
		SAME,
		SEGMENT_LIMIT,
		SELECT,
		SEQUENCE,
		SEQUENTIAL,
		SORT_MERGE,
		STANDARD_1,
		TAPE,
		WORDS,
		PROCESSING,
		APPLY,
		WRITE_ONLY,
		COMMON,
		ALPHABET,
		PADDING,
		SYMBOLIC,
		STANDARD_2,
		OVERRIDE,
		PASSWORD,
		
		//<6.1>
		XML_SCHEMA,
		//</6.1>
		
		ARE_or_IS,
		ASCENDING,
		AT,
		BY,
		CHARACTERS,
		CONTAINS,
		COUNT,
		DEBUGGING,
		DEPENDING,
		DESCENDING,
		DIVISION,
		FOR,
		ORDER,
		INPUT,
		REPLACE,
		KEY,
		LINE_or_LINES,
		
		//<6.1>
		XML_INFORMATION,
		//</6.1>
		
		OF,
		ON,
		OUTPUT,
		RECORD,
		RECORDS,
		REEL,
		SECTION,
		SIZE,
		STATUS,
		THROUGH_or_THRU,
		TIME,
		TIMES,
		TO,
		UNIT,
		USING,
		WHEN,
		WITH,

		//<6.1>
		SQLIMS,
		DEFAULT,
		//</6.1>
		
		PROCEDURE,
		DECLARATIVES,
		END,
		DATA,
		FILE,
		FD,
		SD,
		WORKING_STORAGE,
		LOCAL_STORAGE,
		LINKAGE,
		ENVIRONMENT,
		CONFIGURATION,
		SOURCE_COMPUTER,
		OBJECT_COMPUTER,
		SPECIAL_NAMES,
		REPOSITORY,
		INPUT_OUTPUT,
		FILE_CONTROL,
		I_O_CONTROL,
		ID_or_IDENTIFICATION,
		PROGRAM_ID,
		AUTHOR,
		INSTALLATION,
		DATE_WRITTEN,
		DATE_COMPILED,
		SECURITY,
		CLASS_ID,
		METHOD_ID,
		METHOD,
		FACTORY,
		OBJECT,
		TRACE,
		
		//<6.1>
		SUPPRESS,
		//</6.1>
		
		DATADEF,
		F_NAME,
		UPSI_SWITCH,
		CONDNAME,
		CONDVAR,
		BLOB,
		CLOB,
		DBCLOB,
		BLOB_LOCATOR,
		CLOB_LOCATOR,
		DBCLOB_LOCATOR,
		BLOB_FILE,
		CLOB_FILE,
		DBCLOB_FILE,
		DFHRESP,
		PARSE,
		AUTOMATIC,
		PREVIOUS,
		
		//<6.1>
		ENCODING,
		NAMESPACE,
		NAMESPACE_PREFIX,
		XML_DECLARATION,
		ATTRIBUTES,
		VALIDATING,
		UNBOUNDED,
		ATTRIBUTE,
		ELEMENT,
		NONNUMERIC,
		NAME,
		CYCLE,
		PARAGRAPH,
		AS,
		INITIALIZED,
		//</6.1>
		
		COBOL,
		;
		
		static final Decoder<Integer, TokenCode> decoder =
				new ReversibleMap<Integer, TokenCode>()
				.set(0, PICTURE_STRING)
				.set(3333, TOKEN_PIECE)
				.set(1, ACCEPT)
				.set(2, ADD)
				.set(3, ALTER)
				.set(4, CALL)
				.set(5, CANCEL)
				.set(7, CLOSE)
				.set(9, COMPUTE)
				.set(11, DELETE)
				.set(13, DISPLAY)
				.set(14, DIVIDE)
				.set(17, READY)
				.set(18, END_PERFORM)
				.set(19, ENTER)
				.set(20, ENTRY)
				.set(21, EXIT)
				.set(22, EXEC_or_EXECUTE)
				.set(23, GO)
				.set(24, IF)
				.set(25, INITIALIZE)
				.set(26, INVOKE)
				.set(27, INSPECT)
				.set(28, MERGE)
				.set(29, MOVE)
				.set(30, MULTIPLY)
				.set(31, OPEN)
				.set(32, PERFORM)
				.set(33, READ)
				.set(35, RELEASE)
				.set(36, RETURN)
				.set(37, REWRITE)
				.set(38, SEARCH)
				.set(40, SET)
				.set(41, SORT)
				.set(42, START)
				.set(43, STOP)
				.set(44, STRING)
				.set(45, SUBTRACT)
				.set(48, UNSTRING)
				.set(49, USE)
				.set(50, WRITE)
				.set(51, CONTINUE)
				.set(52, END_ADD)
				.set(53, END_CALL)
				.set(54, END_COMPUTE)
				.set(55, END_DELETE)
				.set(56, END_DIVIDE)
				.set(57, END_EVALUATE)
				.set(58, END_IF)
				.set(59, END_MULTIPLY)
				.set(60, END_READ)
				.set(61, END_RETURN)
				.set(62, END_REWRITE)
				.set(63, END_SEARCH)
				.set(64, END_START)
				.set(65, END_STRING)
				.set(66, END_SUBTRACT)
				.set(67, END_UNSTRING)
				.set(68, END_WRITE)
				.set(69, GOBACK)
				.set(70, EVALUATE)
				.set(71, RESET)
				.set(72, SERVICE)
				.set(73, END_INVOKE)
				.set(74, END_EXEC)
				.set(75, XML)
				.set(76, END_XML)
				.set(77, ALLOCATE)
				.set(78, FREE)
				.set(79, JSON)
				.set(80, END_JSON)
				.set(99, FOREIGN_VERB)
				.set(101, DATA_NAME)
				.set(105, DASHED_NUM)
				.set(106, DECIMAL)
				.set(107, DIV_SIGN)
				.set(108, EQ)
				.set(109, EXPONENTIATION)
				.set(110, GT)
				.set(111, INTEGER)
				.set(112, LT)
				.set(113, LPAREN)
				.set(114, MINUS_SIGN)
				.set(115, MULT_SIGN)
				.set(116, NONUMLIT)
				.set(117, PERIOD)
				.set(118, PLUS_SIGN)
				.set(121, RPAREN)
				.set(122, SIGNED_INTEGER)
				.set(123, QUID)
				.set(124, COLON)
				.set(125, IEOF)
				.set(126, EGCS_LIT)
				.set(127, COMMA_SPACE)
				.set(128, SEMICOLON_SPACE)
				.set(129, PROCEDURE_NAME)
				.set(130, FLT_POINT_LIT)
				.set(131, LANGUAGE_ENVIRONMENT)
				.set(132, GE)
				.set(133, IDREF)
				.set(134, EXPREF)
				.set(136, CICS)
				.set(137, NEW)
				.set(138, NATIONAL_LIT)
				.set(200, ADDRESS)
				.set(201, ADVANCING)
				.set(202, AFTER)
				.set(203, ALL)
				.set(204, ALPHABETIC)
				.set(205, ALPHANUMERIC)
				.set(206, ANY)
				.set(207, AND)
				.set(208, ALPHANUMERIC_EDITED)
				.set(209, BEFORE)
				.set(210, BEGINNING)
				.set(211, FUNCTION)
				.set(212, CONTENT)
				.set(213, CORR_or_CORRESPONDING)
				.set(214, DAY)
				.set(215, DATE)
				.set(216, DEBUG_CONTENTS)
				.set(217, DEBUG_ITEM)
				.set(218, DEBUG_LINE)
				.set(219, DEBUG_NAME)
				.set(220, DEBUG_SUB_1)
				.set(221, DEBUG_SUB_2)
				.set(222, DEBUG_SUB_3)
				.set(223, DELIMITED)
				.set(224, DELIMITER)
				.set(225, DOWN)
				.set(226, NUMERIC_EDITED)
				.set(227, XML_EVENT)
				.set(228, END_OF_PAGE_or_EOP)
				.set(229, EQUAL)
				.set(230, ERROR)
				.set(231, XML_NTEXT)
				.set(232, EXCEPTION)
				.set(233, EXTEND)
				.set(234, FIRST)
				.set(235, FROM)
				.set(236, GIVING)
				.set(237, GREATER)
				.set(238, I_O)
				.set(239, IN)
				.set(240, INITIAL)
				.set(241, INTO)
				.set(242, INVALID)
				.set(243, SQL)
				.set(244, LESS)
				.set(245, LINAGE_COUNTER)
				.set(246, XML_TEXT)
				.set(247, LOCK)
				.set(248, GENERATE)
				.set(249, NEGATIVE)
				.set(250, NEXT)
				.set(251, NO)
				.set(252, NOT)
				.set(253, NUMERIC)
				.set(254, KANJI)
				.set(255, OR)
				.set(256, OTHER)
				.set(257, OVERFLOW)
				.set(258, PAGE)
				.set(259, CONVERTING)
				.set(260, POINTER)
				.set(261, POSITIVE)
				.set(262, DBCS)
				.set(263, PROCEDURES)
				.set(264, PROCEED)
				.set(265, REFERENCES)
				.set(266, DAY_OF_WEEK)
				.set(267, REMAINDER)
				.set(268, REMOVAL)
				.set(269, REPLACING)
				.set(270, REVERSED)
				.set(271, REWIND)
				.set(272, ROUNDED)
				.set(273, RUN)
				.set(274, SENTENCE)
				.set(275, STANDARD)
				.set(276, RETURN_CODE_etc)
				.set(277, TALLYING)
				.set(278, SUM)
				.set(279, TEST)
				.set(280, THAN)
				.set(281, UNTIL)
				.set(282, UP)
				.set(283, UPON)
				.set(284, VARYING)
				.set(285, RELOAD)
				.set(286, TRUE)
				.set(287, THEN)
				.set(288, RETURNING)
				.set(289, ELSE)
				.set(290, SELF)
				.set(291, SUPER)
				.set(292, WHEN_COMPILED)
				.set(293, ENDING)
				.set(294, FALSE)
				.set(295, REFERENCE)
				.set(296, NATIONAL_EDITED)
				.set(297, COM_REG)
				.set(298, ALPHABETIC_LOWER)
				.set(299, ALPHABETIC_UPPER)
				.set(301, REDEFINES)
				.set(302, OCCURS)
				.set(303, SYNC_or_SYNCHRONIZED)
				.set(304, MORE_LABELS)
				.set(305, JUST_or_JUSTIFIED)
				.set(306, SHIFT_IN)
				.set(307, BLANK)
				.set(308, VALUE)
				.set(309, COMP_or_COMPUTATIONAL)
				.set(310, COMP_1_or_COMPUTATIONAL_1)
				.set(311, COMP_3_or_COMPUTATIONAL_3)
				.set(312, COMP_2_or_COMPUTATIONAL_2)
				.set(313, COMP_4_or_COMPUTATIONAL_4)
				.set(314, DISPLAY_1)
				.set(315, SHIFT_OUT)
				.set(316, INDEX)
				.set(317, USAGE)
				.set(318, SIGN)
				.set(319, LEADING)
				.set(320, SEPARATE)
				.set(321, INDEXED)
				.set(322, LEFT)
				.set(323, RIGHT)
				.set(324, PIC_or_PICTURE)
				.set(325, VALUES)
				.set(326, GLOBAL)
				.set(327, EXTERNAL)
				.set(328, BINARY)
				.set(329, PACKED_DECIMAL)
				.set(330, EGCS)
				.set(331, PROCEDURE_POINTER)
				.set(332, COMP_5_or_COMPUTATIONAL_5)
				.set(333, FUNCTION_POINTER)
				.set(334, TYPE)
				.set(335, JNIENVPTR)
				.set(336, NATIONAL)
				.set(337, GROUP_USAGE)
				.set(342, VOLATILE)
				.set(401, HIGH_VALUE_or_HIGH_VALUES)
				.set(402, LOW_VALUE_or_LOW_VALUES)
				.set(403, QUOTE_or_QUOTES)
				.set(404, SPACE_or_SPACES)
				.set(405, ZERO)
				.set(406, ZEROES_or_ZEROS)
				.set(407, NULL_or_NULLS)
				.set(501, BLOCK)
				.set(502, BOTTOM)
				.set(505, CHARACTER)
				.set(506, CODE)
				.set(507, CODE_SET)
				.set(514, FILLER)
				.set(516, FOOTING)
				.set(520, LABEL)
				.set(521, LENGTH)
				.set(524, LINAGE)
				.set(526, OMITTED)
				.set(531, RENAMES)
				.set(543, TOP)
				.set(545, TRAILING)
				.set(549, RECORDING)
				.set(601, INHERITS)
				.set(603, RECURSIVE)
				.set(701, ACCESS)
				.set(702, ALSO)
				.set(703, ALTERNATE)
				.set(704, AREA_or_AREAS)
				.set(705, ASSIGN)
				.set(707, COLLATING)
				.set(708, COMMA)
				.set(709, CURRENCY)
				.set(710, CLASS)
				.set(711, DECIMAL_POINT)
				.set(712, DUPLICATES)
				.set(713, DYNAMIC)
				.set(714, EVERY)
				.set(716, MEMORY)
				.set(717, MODE)
				.set(718, MODULES)
				.set(719, MULTIPLE)
				.set(720, NATIVE)
				.set(721, OFF)
				.set(722, OPTIONAL)
				.set(723, ORGANIZATION)
				.set(724, POSITION)
				.set(725, PROGRAM)
				.set(726, RANDOM)
				.set(727, RELATIVE)
				.set(728, RERUN)
				.set(729, RESERVE)
				.set(730, SAME)
				.set(731, SEGMENT_LIMIT)
				.set(732, SELECT)
				.set(733, SEQUENCE)
				.set(734, SEQUENTIAL)
				.set(736, SORT_MERGE)
				.set(737, STANDARD_1)
				.set(738, TAPE)
				.set(739, WORDS)
				.set(740, PROCESSING)
				.set(741, APPLY)
				.set(742, WRITE_ONLY)
				.set(743, COMMON)
				.set(744, ALPHABET)
				.set(745, PADDING)
				.set(746, SYMBOLIC)
				.set(747, STANDARD_2)
				.set(748, OVERRIDE)
				.set(750, PASSWORD)
				.set(751, XML_SCHEMA)
				.set(801, ARE_or_IS)
				.set(802, ASCENDING)
				.set(803, AT)
				.set(804, BY)
				.set(805, CHARACTERS)
				.set(806, CONTAINS)
				.set(808, COUNT)
				.set(809, DEBUGGING)
				.set(810, DEPENDING)
				.set(811, DESCENDING)
				.set(812, DIVISION)
				.set(814, FOR)
				.set(815, ORDER)
				.set(816, INPUT)
				.set(817, REPLACE)
				.set(818, KEY)
				.set(819, LINE_or_LINES)
				.set(820, XML_INFORMATION)
				.set(821, OF)
				.set(822, ON)
				.set(823, OUTPUT)
				.set(825, RECORD)
				.set(826, RECORDS)
				.set(827, REEL)
				.set(828, SECTION)
				.set(829, SIZE)
				.set(830, STATUS)
				.set(831, THROUGH_or_THRU)
				.set(832, TIME)
				.set(833, TIMES)
				.set(834, TO)
				.set(836, UNIT)
				.set(837, USING)
				.set(838, WHEN)
				.set(839, WITH)
				.set(840, SQLIMS)
				.set(841, DEFAULT)
				.set(901, PROCEDURE)
				.set(902, DECLARATIVES)
				.set(903, END)
				.set(1001, DATA)
				.set(1002, FILE)
				.set(1003, FD)
				.set(1004, SD)
				.set(1005, WORKING_STORAGE)
				.set(1006, LOCAL_STORAGE)
				.set(1007, LINKAGE)
				.set(1101, ENVIRONMENT)
				.set(1102, CONFIGURATION)
				.set(1103, SOURCE_COMPUTER)
				.set(1104, OBJECT_COMPUTER)
				.set(1105, SPECIAL_NAMES)
				.set(1106, REPOSITORY)
				.set(1107, INPUT_OUTPUT)
				.set(1108, FILE_CONTROL)
				.set(1109, I_O_CONTROL)
				.set(1201, ID_or_IDENTIFICATION)
				.set(1202, PROGRAM_ID)
				.set(1203, AUTHOR)
				.set(1204, INSTALLATION)
				.set(1205, DATE_WRITTEN)
				.set(1206, DATE_COMPILED)
				.set(1207, SECURITY)
				.set(1208, CLASS_ID)
				.set(1209, METHOD_ID)
				.set(1210, METHOD)
				.set(1211, FACTORY)
				.set(1212, OBJECT)
				.set(2020, TRACE)
				.set(2046, SUPPRESS)
				.set(3000, DATADEF)
				.set(3001, F_NAME)
				.set(3002, UPSI_SWITCH)
				.set(3003, CONDNAME)
				.set(3004, CONDVAR)
				.set(3005, BLOB)
				.set(3006, CLOB)
				.set(3007, DBCLOB)
				.set(3008, BLOB_LOCATOR)
				.set(3009, CLOB_LOCATOR)
				.set(3010, DBCLOB_LOCATOR)
				.set(3011, BLOB_FILE)
				.set(3012, CLOB_FILE)
				.set(3013, DBCLOB_FILE)
				.set(3014, DFHRESP)
				.set(5001, PARSE)
				.set(5002, AUTOMATIC)
				.set(5003, PREVIOUS)
				.set(5004, ENCODING)
				.set(5005, NAMESPACE)
				.set(5006, NAMESPACE_PREFIX)
				.set(5007, XML_DECLARATION)
				.set(5008, ATTRIBUTES)
				.set(5009, VALIDATING)
				.set(5010, UNBOUNDED)
				.set(5011, ATTRIBUTE)
				.set(5012, ELEMENT)
				.set(5013, NONNUMERIC)
				.set(5014, NAME)
				.set(5015, CYCLE)
				.set(5016, PARAGRAPH)
				.set(5020, AS)
				.set(5021, INITIALIZED)
				.set(9999, COBOL)
				;
		
	}
	
}