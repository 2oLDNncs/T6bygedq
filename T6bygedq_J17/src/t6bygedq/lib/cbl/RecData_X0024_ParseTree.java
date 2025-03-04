package t6bygedq.lib.cbl;

import t6bygedq.lib.Helpers;

/**
 * @author 2oLDNncs 20250203
 */
public final class RecData_X0024_ParseTree extends RecData {
	
	public final LongVar              vNodeNumber            = this.newLongVar(NODE_NUMBER);
	public final EnumVar<NodeType>    vNodeType              = this.newEnumVar(NODE_TYPE, NodeType.decoder);
	public final EnumVar<NodeSubtype> vNodeSubtype           = this.newEnumVar(NODE_SUBTYPE, new SuppliedDecoder<>(() -> this.vNodeType.get().subtypeDecoder));
	public final LongVar              vParentNodeNumber      = this.newLongVar(PARENT_NODE_NUMBER);
	public final LongVar              vLeftSIblingNodeNumber = this.newLongVar(LEFT_SIBLING_NODE_NUMBER);
	public final LongVar              vSymbolId              = this.newLongVar(SYMBOL_ID);
	public final LongVar              vSectionSymbolId       = this.newLongVar(SECTION_SYMBOL_ID);
	public final LongVar              vFirstTokenNumber      = this.newLongVar(FIRST_TOKEN_NUMBER);
	public final LongVar              vLastTokenNumber       = this.newLongVar(LAST_TOKEN_NUMBER);
	public final EnumVar<Flags>       vFlags                 = this.newEnumVar(FLAGS, Flags.decoder);
	
	public RecData_X0024_ParseTree(final Buffer buffer) {
		super(buffer);
	}
	
	private static final Buffer.Region.Generator staticRegionGenerator =
			getStaticRegionGenerator(RecData_X0024_ParseTree.class);
	
	private static final Buffer.Region NODE_NUMBER              = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region NODE_TYPE                = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region NODE_SUBTYPE             = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region PARENT_NODE_NUMBER       = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region LEFT_SIBLING_NODE_NUMBER = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region SYMBOL_ID                = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region SECTION_SYMBOL_ID        = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region FIRST_TOKEN_NUMBER       = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region LAST_TOKEN_NUMBER        = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region RESERVED_01              = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region FLAGS                    = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region RESERVED_02              = staticRegionGenerator.newFixedLength(3);
	
	static {
		Helpers.ignore(RESERVED_01);
		Helpers.ignore(RESERVED_02);
	}
	
	/**
	 * @author 2oLDNncs 20250209
	 */
	public static enum Flags {
		
		NOFLAGS,
		RESERVED_01,
		GENERATED_NODE_NO_TOKEN,
		;
		
		static final Decoder<Integer, Flags> decoder =
				new ReversibleMap<Integer, Flags>()
				.set(0x00, NOFLAGS)
				.set(0x80, RESERVED_01)
				.set(0x40, GENERATED_NODE_NO_TOKEN)
				;
		
	}
	
	/**
	 * @author 2oLDNncs 20250209
	 */
	public static enum NodeType {
		
		PROGRAM,
		CLASS,
		METHOD,
		IDENTIFICATION_DIVISION,
		ENVIRONMENT_DIVISION,
		DATA_DIVISION,
		PROCEDURE_DIVISION,
		END_PROGRAM_METHOD_CLASS,
		DECLARATIVES_BODY,
		NONDECLARATIVES_BODY,
		SECTION(NodeSubtype.decoder1),
		PROCEDURE_SECTION,
		PARAGRAPH(NodeSubtype.decoder2),
		PROCEDURE_PARAGRAPH,
		SENTENCE,
		FILE_DEFINITION,
		SORT_FILE_DEFINITION,
		PROGRAM_NAME,
		PROGRAM_ATTRIBUTE,
		ENVIRONMENT_DIVISION_CLAUSE(NodeSubtype.decoder3),
		CLASS_ATTRIBUTE,
		METHOD_ATTRIBUTE,
		USE_STATEMENT,
		STATEMENT(NodeSubtype.decoder6),
		DATA_DESCRIPTION_CLAUSE(NodeSubtype.decoder4),
		DATA_ENTRY,
		FILE_DESCRIPTION_CLAUSE(NodeSubtype.decoder5),
		DATA_ENTRY_NAME,
		DATA_ENTRY_LEVEL,
		EXEC_ENTRY,
		EVALUATE_SUBJECT_PHRASE(NodeSubtype.decoder7),
		EVALUATE_WHEN_PHRASE(NodeSubtype.decoder7),
		EVALUATE_WHEN_OTHER_PHRASE(NodeSubtype.decoder7),
		SEARCH_WHEN_PHRASE(NodeSubtype.decoder7),
		INSPECT_CONVERTING_PHRASE(NodeSubtype.decoder7),
		INSPECT_REPLACING_PHRASE(NodeSubtype.decoder7),
		INSPECT_TALLYING_PHRASE(NodeSubtype.decoder7),
		PERFORM_UNTIL_PHRASE(NodeSubtype.decoder7),
		PERFORM_VARYING_PHRASE(NodeSubtype.decoder7),
		PERFORM_AFTER_PHRASE(NodeSubtype.decoder7),
		STATEMENT_BLOCK,
		SCOPE_TERMINATOR,
		INITIALIZE_REPLACING_PHRASE(NodeSubtype.decoder7),
		EXEC_CICS_COMMAND,
		
		// <6.1>
		INITIALIZE_WITH_FILLER,
		INITIALIZE_TO_VALUE,
		INITIALIZE_TO_DEFAULT,
		ALLOCATE_INITIALIZED,
		ALLOCATE_LOC,
		// </6.1>
		
		DATA_DIVISION_PHRASE(NodeSubtype.decoder7),
		PHRASE(NodeSubtype.decoder7),
		ON_PHRASE(NodeSubtype.decoder7),
		NOT_PHRASE(NodeSubtype.decoder7),
		THEN_PHRASE(NodeSubtype.decoder7),
		ELSE_PHRASE(NodeSubtype.decoder7),
		CONDITION,
		EXPRESSION,
		RELATIVE_INDEXING,
		EXEC_CICS_OPTION,
		RESERVED_WORD(NodeSubtype.decoder11),
		INITIALIZE_REPLACING_CATEGORY,
		SECTION_OR_PARAGRAPH_NAME,
		IDENTIFIER(NodeSubtype.decoder12),
		ALPHABET_NAME,
		CLASS_NAME,
		CONDITION_NAME(NodeSubtype.decoder12),
		FILE_NAME,
		INDEX_NAME(NodeSubtype.decoder12),
		MNEMONIC_NAME(NodeSubtype.decoder12),
		SYMBOLIC_CHARACTER,
		LITERAL,
		FUNCTION_IDENTIFIER(NodeSubtype.decoder8),
		DATA_NAME(NodeSubtype.decoder12),
		SPECIAL_REGISTER(NodeSubtype.decoder9),
		PROCEDURE_REFERENCE(NodeSubtype.decoder14),
		ARITHMETIC_OPERATOR(NodeSubtype.decoder16),
		ALL_PROCEDURES,
		INITIALIZE_LITERAL_NO_TOKENS(NodeSubtype.decoder13),
		ALL_LITERAL_OR_FIGCON,
		KEYWORD_CLASS_TEST_NAME(NodeSubtype.decoder10),
		RESERVED_WORD_AT_IDENTIFIER_LEVEL(NodeSubtype.decoder15),
		UNARY_OPERATOR,
		RELATIONAL_OPERATOR(NodeSubtype.decoder17),
		SUBSCRIPT,
		REFERENCE_MODIFICATION,
		;
		
		final Decoder<Integer, NodeSubtype> subtypeDecoder;
		
		private NodeType() {
			this(NodeSubtype.decoder0);
		}
		
		private NodeType(final Decoder<Integer, NodeSubtype> subtypeDecoder) {
			this.subtypeDecoder = subtypeDecoder;
		}
		
		static final Decoder<Integer, NodeType> decoder =
				new ReversibleMap<Integer, NodeType>()
				.set(1, PROGRAM)
				.set(2, CLASS)
				.set(3, METHOD)
				.set(101, IDENTIFICATION_DIVISION)
				.set(102, ENVIRONMENT_DIVISION)
				.set(103, DATA_DIVISION)
				.set(104, PROCEDURE_DIVISION)
				.set(105, END_PROGRAM_METHOD_CLASS)
				.set(201, DECLARATIVES_BODY)
				.set(202, NONDECLARATIVES_BODY)
				.set(301, SECTION)
				.set(302, PROCEDURE_SECTION)
				.set(401, PARAGRAPH)
				.set(402, PROCEDURE_PARAGRAPH)
				.set(501, SENTENCE)
				.set(502, FILE_DEFINITION)
				.set(503, SORT_FILE_DEFINITION)
				.set(504, PROGRAM_NAME)
				.set(505, PROGRAM_ATTRIBUTE)
				.set(508, ENVIRONMENT_DIVISION_CLAUSE)
				.set(509, CLASS_ATTRIBUTE)
				.set(510, METHOD_ATTRIBUTE)
				.set(511, USE_STATEMENT)
				.set(601, STATEMENT)
				.set(602, DATA_DESCRIPTION_CLAUSE)
				.set(603, DATA_ENTRY)
				.set(604, FILE_DESCRIPTION_CLAUSE)
				.set(605, DATA_ENTRY_NAME)
				.set(606, DATA_ENTRY_LEVEL)
				.set(607, EXEC_ENTRY)
				.set(701, EVALUATE_SUBJECT_PHRASE)
				.set(702, EVALUATE_WHEN_PHRASE)
				.set(703, EVALUATE_WHEN_OTHER_PHRASE)
				.set(704, SEARCH_WHEN_PHRASE)
				.set(705, INSPECT_CONVERTING_PHRASE)
				.set(706, INSPECT_REPLACING_PHRASE)
				.set(707, INSPECT_TALLYING_PHRASE)
				.set(708, PERFORM_UNTIL_PHRASE)
				.set(709, PERFORM_VARYING_PHRASE)
				.set(710, PERFORM_AFTER_PHRASE)
				.set(711, STATEMENT_BLOCK)
				.set(712, SCOPE_TERMINATOR)
				.set(713, INITIALIZE_REPLACING_PHRASE)
				.set(714, EXEC_CICS_COMMAND)
				.set(715, INITIALIZE_WITH_FILLER)
				.set(716, INITIALIZE_TO_VALUE)
				.set(717, INITIALIZE_TO_DEFAULT)
				.set(718, ALLOCATE_INITIALIZED)
				.set(719, ALLOCATE_LOC)
				.set(720, DATA_DIVISION_PHRASE)
				.set(801, PHRASE)
				.set(802, ON_PHRASE)
				.set(803, NOT_PHRASE)
				.set(804, THEN_PHRASE)
				.set(805, ELSE_PHRASE)
				.set(806, CONDITION)
				.set(807, EXPRESSION)
				.set(808, RELATIVE_INDEXING)
				.set(809, EXEC_CICS_OPTION)
				.set(810, RESERVED_WORD)
				.set(811, INITIALIZE_REPLACING_CATEGORY)
				.set(901, SECTION_OR_PARAGRAPH_NAME)
				.set(902, IDENTIFIER)
				.set(903, ALPHABET_NAME)
				.set(904, CLASS_NAME)
				.set(905, CONDITION_NAME)
				.set(906, FILE_NAME)
				.set(907, INDEX_NAME)
				.set(908, MNEMONIC_NAME)
				.set(910, SYMBOLIC_CHARACTER)
				.set(911, LITERAL)
				.set(912, FUNCTION_IDENTIFIER)
				.set(913, DATA_NAME)
				.set(914, SPECIAL_REGISTER)
				.set(915, PROCEDURE_REFERENCE)
				.set(916, ARITHMETIC_OPERATOR)
				.set(917, ALL_PROCEDURES)
				.set(918, INITIALIZE_LITERAL_NO_TOKENS)
				.set(919, ALL_LITERAL_OR_FIGCON)
				.set(920, KEYWORD_CLASS_TEST_NAME)
				.set(921, RESERVED_WORD_AT_IDENTIFIER_LEVEL)
				.set(922, UNARY_OPERATOR)
				.set(923, RELATIONAL_OPERATOR)
				.set(1001, SUBSCRIPT)
				.set(1002, REFERENCE_MODIFICATION)
				;
		
	}
	
	/**
	 * @author 2oLDNncs 20250209
	 */
	public static enum NodeSubtype {
		
		NONE,
		CONFIGURATION_SECTION,
		INPUT_OUTPUT_SECTION,
		FILE_SECTION,
		WORKING_STORAGE_SECTION,
		LINKAGE_SECTION,
		LOCAL_STORAGE_SECTION,
		REPOSITORY_SECTION,
		PROGRAM_ID_PARAGRAPH,
		AUTHOR_PARAGRAPH,
		INSTALLATION_PARAGRAPH,
		DATE_WRITTEN_PARAGRAPH,
		SECURITY_PARAGRAPH,
		SOURCE_COMPUTER_PARAGRAPH,
		OBJECT_COMPUTER_PARAGRAPH,
		SPECIAL_NAMES_PARAGRAPH,
		FILE_CONTROL_PARAGRAPH,
		I_O_CONTROL_PARAGRAPH,
		DATE_COMPILED_PARAGRAPH,
		CLASS_ID_PARAGRAPH,
		METHOD_ID_PARAGRAPH,
		REPOSITORY_PARAGRAPH,
		WITH_DEBUGGING_MODE,
		MEMORY_SIZE,
		SEGMENT_LIMIT,
		CURRENCY_SIGN,
		DECIMAL_POINT,
		PROGRAM_COLLATING_SEQUENCE,
		ALPHABET,
		SYMBOLIC_CHARACTER,
		CLASS,
		ENVIRONMENT_NAME,
		SELECT,
		XML_SCHEMA,
		BLANK_WHEN_ZERO,
		DATA_NAME_OR_FILLER,
		JUSTIFIED,
		OCCURS,
		PICTURE,
		REDEFINES,
		RENAMES,
		SIGN,
		SYNCHRONIZED,
		USAGE,
		VALUE,
		VOLATILE,
		GLOBAL_4,
		EXTERNAL_4,
		FILE_STATUS,
		ORGANIZATION,
		ACCESS_MODE,
		RECORD_KEY,
		ASSIGN,
		RELATIVE_KEY,
		PASSWORD,
		PROCESSING_MODE,
		RECORD_DELIMITER,
		PADDING_CHARACTER,
		BLOCK_CONTAINS,
		RECORD_CONTAINS,
		LABEL_RECORDS,
		VALUE_OF,
		DATA_RECORDS,
		LINAGE,
		ALTERNATE_KEY,
		LINES_AT_TOP_5,
		LINES_AT_BOTTOM_5,
		CODE_SET,
		RECORDING_MODE,
		RESERVE,
		GLOBAL_5,
		EXTERNAL_5,
		LOCK,
		NEXT_SENTENCE,
		ACCEPT,
		ADD,
		ALTER,
		CALL,
		CANCEL,
		CLOSE,
		COMPUTE,
		CONTINUE,
		DELETE,
		DISPLAY,
		DIVIDE_INTO,
		DIVIDE_BY,
		ENTER,
		ENTRY,
		EVALUATE,
		EXIT,
		GO,
		GOBACK,
		IF,
		INITIALIZE,
		INSPECT,
		INVOKE,
		MERGE,
		MOVE,
		MULTIPLY,
		OPEN,
		PERFORM,
		READ,
		READY,
		RELEASE,
		RESET,
		RETURN,
		REWRITE,
		SEARCH,
		SERVICE,
		SET,
		SORT,
		START,
		STOP,
		STRING,
		SUBTRACT,
		UNSTRING,
		EXEC_SQL,
		EXEC_CICS,
		WRITE,
		XML,
		
		//<6.1>
		ALLOCATE,
		FREE,
		JSON,
		//</6.1>
		
		INTO,
		DELIMITED,
		INITIALIZE_REPLACING,
		INSPECT_ALL,
		INSPECT_LEADING,
		SET_TO,
		SET_UP,
		SET_DOWN,
		PERFORM_TIMES,
		DIVIDE_REMAINDER_7,
		INSPECT_FIRST,
		SEARCH_VARYING,
		MORE_LABELS,
		SEARCH_ALL,
		SEARCH_AT_END,
		SEARCH_TEST_INDEX,
		GLOBAL_7,
		LABEL,
		DEBUGGING,
		SEQUENCE,
		RESERVED_01,
		RESERVED_02,
		RESERVED_03,
		TALLYING,
		RESERVED_04,
		ON_SIZE_ERROR,
		ON_OVERFLOW,
		ON_ERROR,
		AT_END,
		INVALID_KEY,
		END_OF_PAGE,
		USING,
		BEFORE,
		AFTER,
		EXCEPTION,
		CORRESPONDING,
		RESERVED_05,
		RETURNING,
		GIVING,
		THROUGH,
		KEY,
		DELIMITER,
		POINTER,
		COUNT,
		METHOD,
		PROGRAM,
		INPUT,
		OUTPUT,
		I_O,
		EXTEND,
		RELOAD,
		ASCENDING,
		DESCENDING,
		DUPLICATES,
		NATIVE_USAGE,
		INDEXED,
		FROM,
		FOOTING,
		LINES_AT_BOTTOM_7,
		LINES_AT_TOP_7,
		XML_ENCODING,
		XML_GENERATE_XML_DECLARATION,
		XML_GENERATE_ATTRIBUTES,
		XML_GENERATE_NAMESPACE,
		XML_PARSE_PROCESSING,
		XML_PARSE_VALIDATING,
		
		//<6.1>
		XML_GENERATE_NAME,
		XML_GENERATE_TYPE,
		XML_GENERATE_SUPPRESS,
		//</6.1>
		
		COS,
		LOG,
		MAX,
		MIN,
		MOD,
		ORD,
		REM,
		SIN,
		SUM,
		TAN,
		ACOS,
		ASIN,
		ATAN,
		CHAR,
		MEAN,
		SQRT,
		LOG10,
		RANGE,
		LENGTH,
		MEDIAN,
		NUMVAL,
		RANDOM,
		ANNUITY,
		INTEGER,
		ORD_MAX,
		ORD_MIN,
		REVERSE,
		MIDRANGE,
		NUMVAL_C,
		VARIANCE,
		FACTORIAL,
		LOWER_CASE,
		UPPER_CASE,
		CURRENT_DATE,
		INTEGER_PART,
		PRESENT_VALUE,
		WHEN_COMPILED_8,
		DAY_OF_INTEGER,
		INTEGER_OF_DAY,
		DATE_OF_INTEGER,
		INTEGER_OF_DATE,
		STANDARD_DEVIATION,
		YEAR_TO_YYYY,
		DAY_TO_YYYYDDD,
		DATE_TO_YYYYMMDD,
		
		//<4.2>
		UNDATE,
		DATEVAL,
		YEARWINDOW,
		//</4.2>
		
		DISPLAY_OF,
		NATIONAL_OF,
		
		//<6.1>
		UPOS,
		UVALID,
		UWIDTH,
		ULENGTH,
		USUBSTR,
		USUPPLEMENTARY,
		//</6.1>
		
		ADDRESS_OF,
		LENGTH_OF,
		ALPHABETIC_10,
		ALPHABETIC_LOWER,
		ALPHABETIC_UPPER,
		DBCS,
		KANJI,
		NUMERIC,
		NEGATIVE,
		POSITIVE,
		ZERO,
		TRUE_11,
		FALSE,
		ANY,
		THRU,
		REFERENCED,
		CHANGED,
		REFERENCED_CHANGED,
		ALPHABETIC_13,
		ALPHANUMERIC,
		NUMERIC_13,
		ALPHANUMERIC_EDITED,
		NUMERIC_EDITED,
		DBCS_EGCS,
		NATIONAL,
		NATIONAL_EDITED,
		SECTION,
		PARAGRAPH,
		ROUNDED,
		TRUE_15,
		ON,
		OFF,
		SIZE,
		DATE,
		DAY,
		DAY_OF_WEEK,
		TIME,
		WHEN_COMPILED_15,
		PAGE,
		DATE_YYYYMMDD,
		DAY_YYYYDDD,
		
		//<6.1>
		ATTRIBUTE,
		ELEMENT,
		CONTENT,
		NUMERIC_15,
		NONNUMERIC,
		EVERY,
		WHEN,
		//</6.1>
		
		PLUS,
		MINUS,
		TIMES,
		DIVIDE,
		DIVIDE_REMAINDER_16,
		EXPONENTIATE,
		NEGATE,
		LESS,
		LESS_OR_EQUAL,
		EQUAL,
		NOT_EQUAL,
		GREATER,
		GREATER_OR_EQUAL,
		AND,
		OR,
		CLASS_CONDITION,
		NOT_CLASS_CONDITION,
		;
		
		static final Decoder<Integer, NodeSubtype> decoder0 =
				new ReversibleMap<Integer, NodeSubtype>()
				.set(0, NONE)
				;
		
		static final Decoder<Integer, NodeSubtype> decoder1 =
				new ReversibleMap<Integer, NodeSubtype>()
				.set(1, CONFIGURATION_SECTION)
				.set(2, INPUT_OUTPUT_SECTION)
				.set(3, FILE_SECTION)
				.set(4, WORKING_STORAGE_SECTION)
				.set(5, LINKAGE_SECTION)
				.set(6, LOCAL_STORAGE_SECTION)
				.set(7, REPOSITORY_SECTION)
				;
		
		static final Decoder<Integer, NodeSubtype> decoder2 =
				new ReversibleMap<Integer, NodeSubtype>()
				.set(1, PROGRAM_ID_PARAGRAPH)
				.set(2, AUTHOR_PARAGRAPH)
				.set(3, INSTALLATION_PARAGRAPH)
				.set(4, DATE_WRITTEN_PARAGRAPH)
				.set(5, SECURITY_PARAGRAPH)
				.set(6, SOURCE_COMPUTER_PARAGRAPH)
				.set(7, OBJECT_COMPUTER_PARAGRAPH)
				.set(8, SPECIAL_NAMES_PARAGRAPH)
				.set(9, FILE_CONTROL_PARAGRAPH)
				.set(10, I_O_CONTROL_PARAGRAPH)
				.set(11, DATE_COMPILED_PARAGRAPH)
				.set(12, CLASS_ID_PARAGRAPH)
				.set(13, METHOD_ID_PARAGRAPH)
				.set(14, REPOSITORY_PARAGRAPH)
				;
		
		static final Decoder<Integer, NodeSubtype> decoder3 =
				new ReversibleMap<Integer, NodeSubtype>()
				.set(1, WITH_DEBUGGING_MODE)
				.set(2, MEMORY_SIZE)
				.set(3, SEGMENT_LIMIT)
				.set(4, CURRENCY_SIGN)
				.set(5, DECIMAL_POINT)
				.set(6, PROGRAM_COLLATING_SEQUENCE)
				.set(7, ALPHABET)
				.set(8, SYMBOLIC_CHARACTER)
				.set(9, CLASS)
				.set(10, ENVIRONMENT_NAME)
				.set(11, SELECT)
				.set(12, XML_SCHEMA)
				;
		
		static final Decoder<Integer, NodeSubtype> decoder4 =
				new ReversibleMap<Integer, NodeSubtype>()
				.set(0, NONE)
				.set(1, BLANK_WHEN_ZERO)
				.set(2, DATA_NAME_OR_FILLER)
				.set(3, JUSTIFIED)
				.set(4, OCCURS)
				.set(5, PICTURE)
				.set(6, REDEFINES)
				.set(7, RENAMES)
				.set(8, SIGN)
				.set(9, SYNCHRONIZED)
				.set(10, USAGE)
				.set(11, VALUE)
				.set(12, VOLATILE)
				.set(23, GLOBAL_4)
				.set(24, EXTERNAL_4)
				;
		
		static final Decoder<Integer, NodeSubtype> decoder5 =
				new ReversibleMap<Integer, NodeSubtype>()
				.set(1, FILE_STATUS)
				.set(2, ORGANIZATION)
				.set(3, ACCESS_MODE)
				.set(4, RECORD_KEY)
				.set(5, ASSIGN)
				.set(6, RELATIVE_KEY)
				.set(7, PASSWORD)
				.set(8, PROCESSING_MODE)
				.set(9, RECORD_DELIMITER)
				.set(10, PADDING_CHARACTER)
				.set(11, BLOCK_CONTAINS)
				.set(12, RECORD_CONTAINS)
				.set(13, LABEL_RECORDS)
				.set(14, VALUE_OF)
				.set(15, DATA_RECORDS)
				.set(16, LINAGE)
				.set(17, ALTERNATE_KEY)
				.set(18, LINES_AT_TOP_5)
				.set(19, LINES_AT_BOTTOM_5)
				.set(20, CODE_SET)
				.set(21, RECORDING_MODE)
				.set(22, RESERVE)
				.set(23, GLOBAL_5)
				.set(24, EXTERNAL_5)
				.set(25, LOCK)
				;
		
		static final Decoder<Integer, NodeSubtype> decoder6 =
				new ReversibleMap<Integer, NodeSubtype>()
				.set(2, NEXT_SENTENCE)
				.set(3, ACCEPT)
				.set(4, ADD)
				.set(5, ALTER)
				.set(6, CALL)
				.set(7, CANCEL)
				.set(8, CLOSE)
				.set(9, COMPUTE)
				.set(10, CONTINUE)
				.set(11, DELETE)
				.set(12, DISPLAY)
				.set(13, DIVIDE_INTO)
				.set(113, DIVIDE_BY)
				.set(14, ENTER)
				.set(15, ENTRY)
				.set(16, EVALUATE)
				.set(17, EXIT)
				.set(18, GO)
				.set(19, GOBACK)
				.set(20, IF)
				.set(21, INITIALIZE)
				.set(22, INSPECT)
				.set(23, INVOKE)
				.set(24, MERGE)
				.set(25, MOVE)
				.set(26, MULTIPLY)
				.set(27, OPEN)
				.set(28, PERFORM)
				.set(29, READ)
				.set(30, READY)
				.set(31, RELEASE)
				.set(32, RESET)
				.set(33, RETURN)
				.set(34, REWRITE)
				.set(35, SEARCH)
				.set(36, SERVICE)
				.set(37, SET)
				.set(38, SORT)
				.set(39, START)
				.set(40, STOP)
				.set(41, STRING)
				.set(42, SUBTRACT)
				.set(43, UNSTRING)
				.set(44, EXEC_SQL)
				.set(144, EXEC_CICS)
				.set(45, WRITE)
				.set(46, XML)
				.set(47, ALLOCATE)
				.set(48, FREE)
				.set(49, JSON)
				;
		
		static final Decoder<Integer, NodeSubtype> decoder7 =
				new ReversibleMap<Integer, NodeSubtype>()
				.set(0, NONE)
				.set(1, INTO)
				.set(2, DELIMITED)
				.set(3, INITIALIZE_REPLACING)
				.set(4, INSPECT_ALL)
				.set(5, INSPECT_LEADING)
				.set(6, SET_TO)
				.set(7, SET_UP)
				.set(8, SET_DOWN)
				.set(9, PERFORM_TIMES)
				.set(10, DIVIDE_REMAINDER_7)
				.set(11, INSPECT_FIRST)
				.set(12, SEARCH_VARYING)
				.set(13, MORE_LABELS)
				.set(14, SEARCH_ALL)
				.set(15, SEARCH_AT_END)
				.set(16, SEARCH_TEST_INDEX)
				.set(17, GLOBAL_7)
				.set(18, LABEL)
				.set(19, DEBUGGING)
				.set(20, SEQUENCE)
				.set(21, RESERVED_01)
				.set(22, RESERVED_02)
				.set(23, RESERVED_03)
				.set(24, TALLYING)
				.set(25, RESERVED_04)
				.set(26, ON_SIZE_ERROR)
				.set(27, ON_OVERFLOW)
				.set(28, ON_ERROR)
				.set(29, AT_END)
				.set(30, INVALID_KEY)
				.set(31, END_OF_PAGE)
				.set(32, USING)
				.set(33, BEFORE)
				.set(34, AFTER)
				.set(35, EXCEPTION)
				.set(36, CORRESPONDING)
				.set(37, RESERVED_05)
				.set(38, RETURNING)
				.set(39, GIVING)
				.set(40, THROUGH)
				.set(41, KEY)
				.set(42, DELIMITER)
				.set(43, POINTER)
				.set(44, COUNT)
				.set(45, METHOD)
				.set(46, PROGRAM)
				.set(47, INPUT)
				.set(48, OUTPUT)
				.set(49, I_O)
				.set(50, EXTEND)
				.set(51, RELOAD)
				.set(52, ASCENDING)
				.set(53, DESCENDING)
				.set(54, DUPLICATES)
				.set(55, NATIVE_USAGE)
				.set(56, INDEXED)
				.set(57, FROM)
				.set(58, FOOTING)
				.set(59, LINES_AT_BOTTOM_7)
				.set(60, LINES_AT_TOP_7)
				.set(61, XML_ENCODING)
				.set(62, XML_GENERATE_XML_DECLARATION)
				.set(63, XML_GENERATE_ATTRIBUTES)
				.set(64, XML_GENERATE_NAMESPACE)
				.set(65, XML_PARSE_PROCESSING)
				.set(66, XML_PARSE_VALIDATING)
				.set(67, XML_GENERATE_NAME)
				.set(68, XML_GENERATE_TYPE)
				.set(69, XML_GENERATE_SUPPRESS)
				;
		
		static final Decoder<Integer, NodeSubtype> decoder8 =
				new ReversibleMap<Integer, NodeSubtype>()
				.set(1, COS)
				.set(2, LOG)
				.set(3, MAX)
				.set(4, MIN)
				.set(5, MOD)
				.set(6, ORD)
				.set(7, REM)
				.set(8, SIN)
				.set(9, SUM)
				.set(10, TAN)
				.set(11, ACOS)
				.set(12, ASIN)
				.set(13, ATAN)
				.set(14, CHAR)
				.set(15, MEAN)
				.set(16, SQRT)
				.set(17, LOG10)
				.set(18, RANGE)
				.set(19, LENGTH)
				.set(20, MEDIAN)
				.set(21, NUMVAL)
				.set(22, RANDOM)
				.set(23, ANNUITY)
				.set(24, INTEGER)
				.set(25, ORD_MAX)
				.set(26, ORD_MIN)
				.set(27, REVERSE)
				.set(28, MIDRANGE)
				.set(29, NUMVAL_C)
				.set(30, VARIANCE)
				.set(31, FACTORIAL)
				.set(32, LOWER_CASE)
				.set(33, UPPER_CASE)
				.set(34, CURRENT_DATE)
				.set(35, INTEGER_PART)
				.set(36, PRESENT_VALUE)
				.set(37, WHEN_COMPILED_8)
				.set(38, DAY_OF_INTEGER)
				.set(39, INTEGER_OF_DAY)
				.set(40, DATE_OF_INTEGER)
				.set(41, INTEGER_OF_DATE)
				.set(42, STANDARD_DEVIATION)
				.set(43, YEAR_TO_YYYY)
				.set(44, DAY_TO_YYYYDDD)
				.set(45, DATE_TO_YYYYMMDD)
				.set(46, UNDATE)
				.set(47, DATEVAL)
				.set(48, YEARWINDOW)
				.set(49, DISPLAY_OF)
				.set(50, NATIONAL_OF)
				.set(51, UPOS)
				.set(52, UVALID)
				.set(53, UWIDTH)
				.set(54, ULENGTH)
				.set(55, USUBSTR)
				.set(56, USUPPLEMENTARY)
				;
		
		static final Decoder<Integer, NodeSubtype> decoder9 =
				new ReversibleMap<Integer, NodeSubtype>()
				.set(1, ADDRESS_OF)
				.set(2, LENGTH_OF)
				;
		
		static final Decoder<Integer, NodeSubtype> decoder10 =
				new ReversibleMap<Integer, NodeSubtype>()
				.set(1, ALPHABETIC_10)
				.set(2, ALPHABETIC_LOWER)
				.set(3, ALPHABETIC_UPPER)
				.set(4, DBCS)
				.set(5, KANJI)
				.set(6, NUMERIC)
				.set(7, NEGATIVE)
				.set(8, POSITIVE)
				.set(9, ZERO)
				;
		
		static final Decoder<Integer, NodeSubtype> decoder11 =
				new ReversibleMap<Integer, NodeSubtype>()
				.set(1, TRUE_11)
				.set(2, FALSE)
				.set(3, ANY)
				.set(4, THRU)
				;
		
		static final Decoder<Integer, NodeSubtype> decoder12 =
				new ReversibleMap<Integer, NodeSubtype>()
				.set(1, REFERENCED)
				.set(2, CHANGED)
				.set(3, REFERENCED_CHANGED)
				;
		
		static final Decoder<Integer, NodeSubtype> decoder13 =
				new ReversibleMap<Integer, NodeSubtype>()
				.set(1, ALPHABETIC_13)
				.set(2, ALPHANUMERIC)
				.set(3, NUMERIC_13)
				.set(4, ALPHANUMERIC_EDITED)
				.set(5, NUMERIC_EDITED)
				.set(6, DBCS_EGCS)
				.set(7, NATIONAL)
				.set(8, NATIONAL_EDITED)
				;
		
		static final Decoder<Integer, NodeSubtype> decoder14 =
				new ReversibleMap<Integer, NodeSubtype>()
				.set(1, SECTION)
				.set(2, PARAGRAPH)
				;
		
		static final Decoder<Integer, NodeSubtype> decoder15 =
				new ReversibleMap<Integer, NodeSubtype>()
				.set(1, ROUNDED)
				.set(2, TRUE_15)
				.set(3, ON)
				.set(4, OFF)
				.set(5, SIZE)
				.set(6, DATE)
				.set(7, DAY)
				.set(8, DAY_OF_WEEK)
				.set(9, TIME)
				.set(10, WHEN_COMPILED_15)
				.set(11, PAGE)
				.set(12, DATE_YYYYMMDD)
				.set(13, DAY_YYYYDDD)
				.set(14, ATTRIBUTE)
				.set(15, ELEMENT)
				.set(16, CONTENT)
				.set(17, NUMERIC_15)
				.set(18, NONNUMERIC)
				.set(19, EVERY)
				.set(20, WHEN)
				;
		
		static final Decoder<Integer, NodeSubtype> decoder16 =
				new ReversibleMap<Integer, NodeSubtype>()
				.set(1, PLUS)
				.set(2, MINUS)
				.set(3, TIMES)
				.set(4, DIVIDE)
				.set(5, DIVIDE_REMAINDER_16)
				.set(6, EXPONENTIATE)
				.set(7, NEGATE)
				;
		
		static final Decoder<Integer, NodeSubtype> decoder17 =
				new ReversibleMap<Integer, NodeSubtype>()
				.set(8, LESS)
				.set(9, LESS_OR_EQUAL)
				.set(10, EQUAL)
				.set(11, NOT_EQUAL)
				.set(12, GREATER)
				.set(13, GREATER_OR_EQUAL)
				.set(14, AND)
				.set(15, OR)
				.set(16, CLASS_CONDITION)
				.set(17, NOT_CLASS_CONDITION)
				;
		
	}
	
}