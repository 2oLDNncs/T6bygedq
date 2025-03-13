package t6bygedq.lib.cbl;

import t6bygedq.lib.Helpers;

/**
 * @author 2oLDNncs 20250203
 */
public final class RecData_X0042_Symbol_4_2 extends RecData {
	
	public final LongVar                            vSymbolId                  = this.newLongVar(SYMBOL_ID);
	public final LongVar                            vLineNumber                = this.newLongVar(LINE_NUMBER);
	public final IntVar                             vLevel                     = this.newIntVar(LEVEL);
	public final EnumVar<QualificationIndicator>    vQualificationIndicator    = this.newEnumVar(QUALIFICATION_INDICATOR, QualificationIndicator.decoder);
	public final EnumVar<SymbolType>                vSymbolType                = this.newEnumVar(SYMBOL_TYPE, SymbolType.decoder);
	public final BooleanVar                         vExternal                  = this.newBooleanVar(SYMBOL_TYPE, 5);
	public final BooleanVar                         vGlobal                    = this.newBooleanVar(SYMBOL_TYPE, 6);
	public final EnumVar<SymbolAttribute>           vSymbolAttribute           = this.newEnumVar(SYMBOL_ATTRIBUTE, SymbolAttribute.decoder);
	public final BooleanVar                         vValue                     = this.newBooleanVar(CLAUSES, 0);
	public final BooleanVar                         vIndexed                   = this.newBooleanVar(CLAUSES, 1);
	public final BooleanVar                         vRedefines                 = this.newBooleanVar(CLAUSES, 2);
	public final BooleanVar                         vRenames                   = this.newBooleanVar(CLAUSES, 3);
	public final BooleanVar                         vOccurs                    = this.newBooleanVar(CLAUSES, 4);
	public final BooleanVar                         vHasOccursKeys             = this.newBooleanVar(CLAUSES, 5);
	public final BooleanVar                         vOccursDependingOn         = this.newBooleanVar(CLAUSES, 6);
	public final BooleanVar                         vOccursInParent            = this.newBooleanVar(CLAUSES, 7);
	public final BooleanVar                         vSelect                    = this.newBooleanVar(CLAUSES, 0);
	public final BooleanVar                         vAssign                    = this.newBooleanVar(CLAUSES, 1);
	public final BooleanVar                         vRerun                     = this.newBooleanVar(CLAUSES, 2);
	public final BooleanVar                         vSameArea                  = this.newBooleanVar(CLAUSES, 3);
	public final BooleanVar                         vSameRecodArea             = this.newBooleanVar(CLAUSES, 4);
	public final BooleanVar                         vRecordingMode             = this.newBooleanVar(CLAUSES, 5);
	public final BooleanVar                         vRecord                    = this.newBooleanVar(CLAUSES, 7);
//	public final EnumVar<MnemonicNameSymbolClauses> vMnemonicNameSymbolClauses = this.newEnumVar(CLAUSES, MnemonicNameSymbolClauses.decoder);
	public final EnumVar<MnemonicNameSymbolClauses> vMnemonicNameSymbolClauses = this.newEnumVar(CLAUSES, new Decoder<Integer, MnemonicNameSymbolClauses>() {
		
		@Override
		public final MnemonicNameSymbolClauses get(final Integer key) {
			if (SymbolType.MNEMONIC_NAME.equals(vSymbolType.get())) {
				return MnemonicNameSymbolClauses.decoder.get(key);
			}
			
			return MnemonicNameSymbolClauses.NONE;
		}
		
		@Override
		public final Integer getKey(final MnemonicNameSymbolClauses value) {
			if (SymbolType.MNEMONIC_NAME.equals(vSymbolType.get())) {
				return MnemonicNameSymbolClauses.decoder.getKey(value);
			}
			
			return 0;
		}
		
	});
	public final BooleanVar                         vRedefined                 = this.newBooleanVar(DATA_FLAGS_1, 0);
	public final BooleanVar                         vRenamed                   = this.newBooleanVar(DATA_FLAGS_1, 1);
	public final BooleanVar                         vSynchronized              = this.newBooleanVar(DATA_FLAGS_1, 2);
	public final BooleanVar                         vImplicitlyRedefined       = this.newBooleanVar(DATA_FLAGS_1, 3);
	public final BooleanVar                         vVolatile                  = this.newBooleanVar(DATA_FLAGS_1, 4);
	public final BooleanVar                         vImplicitlyRedefines       = this.newBooleanVar(DATA_FLAGS_1, 5);
	public final BooleanVar                         vFiller                    = this.newBooleanVar(DATA_FLAGS_1, 6);
	public final BooleanVar                         vLevel77                   = this.newBooleanVar(DATA_FLAGS_1, 7);
	public final BooleanVar                         vBinary                    = this.newBooleanVar(DATA_FLAGS_2, 0);
	public final BooleanVar                         vExternalFloatingPoint     = this.newBooleanVar(DATA_FLAGS_2, 1);
	public final BooleanVar                         vInternalFloatingPoint     = this.newBooleanVar(DATA_FLAGS_2, 2);
	public final BooleanVar                         vPacked                    = this.newBooleanVar(DATA_FLAGS_2, 3);
	public final BooleanVar                         vExternalDecimal           = this.newBooleanVar(DATA_FLAGS_2, 4);
	public final BooleanVar                         vScaledNegative            = this.newBooleanVar(DATA_FLAGS_2, 5);
	public final BooleanVar                         vNumericEdited             = this.newBooleanVar(DATA_FLAGS_2, 6);
	public final BooleanVar                         vAlphabetic                = this.newBooleanVar(DATA_FLAGS_2, 0);
	public final BooleanVar                         vAlphanumeric              = this.newBooleanVar(DATA_FLAGS_2, 1);
	public final BooleanVar                         vAlphanumericEdited        = this.newBooleanVar(DATA_FLAGS_2, 2);
	public final BooleanVar                         vGroupContainsItsOwnOdoObj = this.newBooleanVar(DATA_FLAGS_2, 3);
	public final BooleanVar                         vDbcsItem                  = this.newBooleanVar(DATA_FLAGS_2, 4);
	public final BooleanVar                         vGroupVariableLength       = this.newBooleanVar(DATA_FLAGS_2, 5);
	public final BooleanVar                         vEgcsItem                  = this.newBooleanVar(DATA_FLAGS_2, 6);
	public final BooleanVar                         vEgcsEdited                = this.newBooleanVar(DATA_FLAGS_2, 7);
	public final BooleanVar                         vObjecOfOdoInRecord        = this.newBooleanVar(DATA_FLAGS_2, 0);
	public final BooleanVar                         vSubjecOfOdoInRecord       = this.newBooleanVar(DATA_FLAGS_2, 1);
	public final BooleanVar                         vSequentialAccess          = this.newBooleanVar(DATA_FLAGS_2, 2);
	public final BooleanVar                         vRandomAccess              = this.newBooleanVar(DATA_FLAGS_2, 3);
	public final BooleanVar                         vDynamicAccess             = this.newBooleanVar(DATA_FLAGS_2, 4);
	public final BooleanVar                         vLocateMode                = this.newBooleanVar(DATA_FLAGS_2, 5);
	public final BooleanVar                         vRecordArea                = this.newBooleanVar(DATA_FLAGS_2, 6);
	public final BooleanVar                         vAllRecordsAreSameLength   = this.newBooleanVar(DATA_FLAGS_3, 0);
	public final BooleanVar                         vFixedLength               = this.newBooleanVar(DATA_FLAGS_3, 1);
	public final BooleanVar                         vVariableLength            = this.newBooleanVar(DATA_FLAGS_3, 2);
	public final BooleanVar                         vUndefined                 = this.newBooleanVar(DATA_FLAGS_3, 3);
	public final BooleanVar                         vSpanned                   = this.newBooleanVar(DATA_FLAGS_3, 4);
	public final BooleanVar                         vBlocked                   = this.newBooleanVar(DATA_FLAGS_3, 5);
	public final BooleanVar                         vApplyWriteOnly            = this.newBooleanVar(DATA_FLAGS_3, 6);
	public final BooleanVar                         vSameSortMergeArea         = this.newBooleanVar(DATA_FLAGS_3, 7);
	public final BooleanVar                         vPhysicalSequential        = this.newBooleanVar(FILE_ORGANIZATION_AND_ATTRIBUTES, 0);
	public final BooleanVar                         vAscii                     = this.newBooleanVar(FILE_ORGANIZATION_AND_ATTRIBUTES, 1);
	public final BooleanVar                         vStandardLabel             = this.newBooleanVar(FILE_ORGANIZATION_AND_ATTRIBUTES, 2);
	public final BooleanVar                         vUserLabel                 = this.newBooleanVar(FILE_ORGANIZATION_AND_ATTRIBUTES, 3);
	public final BooleanVar                         vSequentialOrganization    = this.newBooleanVar(FILE_ORGANIZATION_AND_ATTRIBUTES, 4);
	public final BooleanVar                         vIndexedOrganization       = this.newBooleanVar(FILE_ORGANIZATION_AND_ATTRIBUTES, 5);
	public final BooleanVar                         vRelativeOrganization      = this.newBooleanVar(FILE_ORGANIZATION_AND_ATTRIBUTES, 6);
	public final BooleanVar                         vLineSequential            = this.newBooleanVar(FILE_ORGANIZATION_AND_ATTRIBUTES, 7);
	public final EnumVar<UsageClause>               vUsageClause               = this.newEnumVar(USAGE_CLAUSE, UsageClause.decoder);
	public final EnumVar<SignClause>                vSignClause                = this.newEnumVar(SIGN_CLAUSE, SignClause.decoder);
	public final BooleanVar                         vJustified                 = this.newBooleanVar(INDICATORS, 7);
	public final BooleanVar                         vBlankWhenZero             = this.newBooleanVar(INDICATORS, 6);
	public final LongVar                            vSize                      = this.newLongVar(SIZE);
	public final IntVar                             vPrecision                 = this.newIntVar(PRECISION);
	public final IntVar                             vScale                     = this.newIntVar(SCALE);
	public final EnumVar<StorageType>               vStorageType               = this.newEnumVar(STORAGE_TYPE, StorageType.decoder);
	public final EnumVar<DateFormat>                vDateFormat                = this.newEnumVar(DATE_FORMAT, DateFormat.decoder);
	public final BooleanVar                         vNumericNational           = this.newBooleanVar(DATA_FLAGS_4, 0);
	public final BooleanVar                         vNational                  = this.newBooleanVar(DATA_FLAGS_4, 0);
	public final BooleanVar                         vNationalEdited            = this.newBooleanVar(DATA_FLAGS_4, 1);
	public final BooleanVar                         vGroupUsageNational        = this.newBooleanVar(DATA_FLAGS_4, 0);
	
	public final LongVar                            vAddressingInformation     = this.newLongVar(ADDRESSING_INFORMATION);
	public final LongVar                            vStructureDisplacement     = this.newLongVar(STRUCTURE_DISPLACEMENT);
	public final LongVar                            vParentDisplacement        = this.newLongVar(PARENT_DISPLACEMENT);
	public final LongVar                            vParentId                  = this.newLongVar(PARENT_ID);
	public final LongVar                            vRedefinedId               = this.newLongVar(REDEFINED_ID);
	public final LongVar                            vStartRenamedId            = this.newLongVar(START_RENAMED_ID);
	public final LongVar                            vEndRenamedId              = this.newLongVar(END_RENAMED_ID);
	public final LongVar                            vProgramNameSymbolId       = this.newLongVar(PROGRAM_NAME_SYMBOL_ID);
	public final LongVar                            vOccursMinimum             = this.newLongVar(OCCURS_MINIMUM_or_PARAGRAPH_ID);
	public final LongVar                            vParagraphId               = this.newLongVar(OCCURS_MINIMUM_or_PARAGRAPH_ID);
	public final LongVar                            vOccursMaximum             = this.newLongVar(OCCURS_MAXIMUM_or_SECTION_ID);
	public final LongVar                            vSectionId                 = this.newLongVar(OCCURS_MAXIMUM_or_SECTION_ID);
	public final LongVar                            vDimensions                = this.newLongVar(DIMENSIONS);
	public final BooleanVar                         vSymbolNameLowercaseChar01 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_1, 0);
	public final BooleanVar                         vSymbolNameLowercaseChar02 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_1, 1);
	public final BooleanVar                         vSymbolNameLowercaseChar03 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_1, 2);
	public final BooleanVar                         vSymbolNameLowercaseChar04 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_1, 3);
	public final BooleanVar                         vSymbolNameLowercaseChar05 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_1, 4);
	public final BooleanVar                         vSymbolNameLowercaseChar06 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_1, 5);
	public final BooleanVar                         vSymbolNameLowercaseChar07 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_1, 6);
	public final BooleanVar                         vSymbolNameLowercaseChar08 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_1, 7);
	public final BooleanVar                         vSymbolNameLowercaseChar09 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_2, 8);
	public final BooleanVar                         vSymbolNameLowercaseChar10 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_2, 9);
	public final BooleanVar                         vSymbolNameLowercaseChar11 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_2, 10);
	public final BooleanVar                         vSymbolNameLowercaseChar12 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_2, 11);
	public final BooleanVar                         vSymbolNameLowercaseChar13 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_2, 12);
	public final BooleanVar                         vSymbolNameLowercaseChar14 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_2, 13);
	public final BooleanVar                         vSymbolNameLowercaseChar15 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_2, 14);
	public final BooleanVar                         vSymbolNameLowercaseChar16 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_2, 15);
	public final BooleanVar                         vSymbolNameLowercaseChar17 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_3, 16);
	public final BooleanVar                         vSymbolNameLowercaseChar18 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_3, 17);
	public final BooleanVar                         vSymbolNameLowercaseChar19 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_3, 18);
	public final BooleanVar                         vSymbolNameLowercaseChar20 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_3, 19);
	public final BooleanVar                         vSymbolNameLowercaseChar21 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_3, 20);
	public final BooleanVar                         vSymbolNameLowercaseChar22 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_3, 21);
	public final BooleanVar                         vSymbolNameLowercaseChar23 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_3, 22);
	public final BooleanVar                         vSymbolNameLowercaseChar24 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_3, 23);
	public final BooleanVar                         vSymbolNameLowercaseChar25 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_4, 24);
	public final BooleanVar                         vSymbolNameLowercaseChar26 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_4, 25);
	public final BooleanVar                         vSymbolNameLowercaseChar27 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_4, 26);
	public final BooleanVar                         vSymbolNameLowercaseChar28 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_4, 27);
	public final BooleanVar                         vSymbolNameLowercaseChar29 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_4, 28);
	public final BooleanVar                         vSymbolNameLowercaseChar30 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_4, 29);
	public final BooleanVar                         vSymbolNameLowercaseChar31 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_4, 30);
	public final BooleanVar                         vSymbolNameLowercaseChar32 = this.newBooleanVar(CASE_BIT_VECTOR_BYTE_4, 31);
	public final LongVar                            vOdoSymbolNameId           = this.newLongVar(ODO_SYMBOL_NAME_ID_or_ASSIGN_DATA_NAME_ID);
	public final LongVar                            vAssignDataNameId          = this.vOdoSymbolNameId;
	
	public final StringVar                          vSymbolName                = this.newStringVarV(SYMBOL_NAME_LENGTH);
	public final StringVar                          vPictureDataString         = this.newStringVarV(PICTURE_DATA_LENGTH_or_ASSIGNMENT_NAME_LENGTH);
	public final StringVar                          vAssignmentName            = this.vPictureDataString;
	public final IntListVar                         vIndexIdList               = this.newIntListVarV(INDEX_COUNT);
//	public final LongListVar                        vKeys                      = this.newLongListVarV(KEYS_COUNT);
	public final ListVar_<Key>                      vKeys                      = this.newListVarV(KEYS_COUNT, Key::new);
	
//	public final StringVar                          vInitialValueData          = this.newStringVarV(INITIAL_VALUE_LENGTH);
	public final ListVar_<Pair>                     vPairs                     = this.newListVarV2(INITIAL_VALUE_LENGTH, Pair::new);
	
	public final StringVar                          vExternalClassName         = this.newStringVarV(EXTERNAL_CLASS_NAME_LENGTH);;
	
	public RecData_X0042_Symbol_4_2(final Buffer buffer) {
		super(buffer);
	}
	
	@Override
	protected final void afterRead() {
		this.vKeys.afterRead();
		this.vPairs.afterRead();
		super.afterRead();
	}
	
	/**
	 * @author 2oLDNncs 20250214
	 */
	public final class Key {
		
		public final EnumVar<KeySequence>           vKeySequence               = newEnumVar(1, KeySequence.decoder);
		private final StringVar                     vReserved02                = newStringVarF(3);
		public final LongVar                        vKeyId                     = newLongVar(4);
		
		{
			Helpers.ignore(this.vReserved02);
		}
		
	}
	
	/**
	 * @author 2oLDNncs 20250214
	 */
	public final class Pair extends RecPart {
		
		private final Buffer.Region firstValueLength;
		public final StringVar firstValue;
		private final Buffer.Region secondValueLength;
		public final StringVar secondValue;
		
		Pair(final Buffer.Region.Generator rg) {
			super(RecData_X0042_Symbol_4_2.this.buffer);
			Buffer.DEBUG = false;
			this.setDynamicRegionGenerator(rg);
			this.firstValueLength = this.newDynamicFixedLengthRegion(2);
			this.firstValue  = this.newStringVarV(this.firstValueLength);
			this.secondValueLength = this.newDynamicFixedLengthRegion(2);
			this.secondValue = this.newStringVarV(this.secondValueLength);
			Buffer.DEBUG = false;
		}
		
	}
	
	private static final Buffer.Region.Generator staticRegionGenerator =
			getStaticRegionGenerator(RecData_X0042_Symbol_4_2.class);
	
	private static final Buffer.Region SYMBOL_ID                                          = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region LINE_NUMBER                                        = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region LEVEL                                              = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region QUALIFICATION_INDICATOR                            = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region SYMBOL_TYPE                                        = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region SYMBOL_ATTRIBUTE                                   = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region CLAUSES                                            = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region DATA_FLAGS_1                                       = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region DATA_FLAGS_2                                       = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region DATA_FLAGS_3                                       = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region FILE_ORGANIZATION_AND_ATTRIBUTES                   = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region USAGE_CLAUSE                                       = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region SIGN_CLAUSE                                        = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region INDICATORS                                         = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region SIZE                                               = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region PRECISION                                          = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region SCALE                                              = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region STORAGE_TYPE                                       = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region DATE_FORMAT                                        = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region DATA_FLAGS_4                                       = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region RESERVED_01                                        = staticRegionGenerator.newFixedLength(3);
	
	private static final Buffer.Region ADDRESSING_INFORMATION                             = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region STRUCTURE_DISPLACEMENT                             = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region PARENT_DISPLACEMENT                                = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region PARENT_ID                                          = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region REDEFINED_ID                                       = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region START_RENAMED_ID                                   = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region END_RENAMED_ID                                     = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region PROGRAM_NAME_SYMBOL_ID                             = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region OCCURS_MINIMUM_or_PARAGRAPH_ID                     = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region OCCURS_MAXIMUM_or_SECTION_ID                       = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region DIMENSIONS                                         = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region CASE_BIT_VECTOR_BYTE_1                             = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region CASE_BIT_VECTOR_BYTE_2                             = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region CASE_BIT_VECTOR_BYTE_3                             = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region CASE_BIT_VECTOR_BYTE_4                             = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region RESERVED_02                                        = staticRegionGenerator.newFixedLength(8);
	private static final Buffer.Region VALUE_PAIRS_COUNT                                  = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region SYMBOL_NAME_LENGTH                                 = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region PICTURE_DATA_LENGTH_or_ASSIGNMENT_NAME_LENGTH      = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region INITIAL_VALUE_LENGTH                               = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region EXTERNAL_CLASS_NAME_LENGTH                         = INITIAL_VALUE_LENGTH;
	private static final Buffer.Region ODO_SYMBOL_NAME_ID_or_ASSIGN_DATA_NAME_ID          = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region KEYS_COUNT                                         = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region INDEX_COUNT                                        = staticRegionGenerator.newFixedLength(2);
	
	static {
		Helpers.ignore(RESERVED_01);
		Helpers.ignore(RESERVED_02);
	}
	
	/**
	 * @author 2oLDNncs 20250209
	 */
	public static enum QualificationIndicator {
		
		NO_QUALIFICATION_NEEDED,
		QUALIFICATION_NEEDED,
		;
		
		static final Decoder<Integer, QualificationIndicator> decoder =
				new ReversibleMap<Integer, QualificationIndicator>()
				.set(0x00, NO_QUALIFICATION_NEEDED)
				.set(0x01, QUALIFICATION_NEEDED)
				;
	}
	
	/**
	 * @author 2oLDNncs 20250209
	 */
	public static enum SymbolType {
		
		CLASS_NAME,
		METHOD_NAME,
		DATA_NAME,
		PROCEDURE_NAME,
		MNEMONIC_NAME,
		PROGRAM_NAME,
		RESERVED_01,
		;
		
		static final Decoder<Integer, SymbolType> decoder =
				new KeyMaskingDecoder<>(new ReversibleMap<Integer, SymbolType>()
						.set(0x68, CLASS_NAME)
						.set(0x58, METHOD_NAME)
						.set(0x40, DATA_NAME)
						.set(0x20, PROCEDURE_NAME)
						.set(0x10, MNEMONIC_NAME)
						.set(0x08, PROGRAM_NAME)
						.set(0x81, RESERVED_01)
						, 0b11111001);
		
	}
	
	/**
	 * @author 2oLDNncs 20250209
	 */
	public static enum SymbolAttribute {
		
		NUMERIC,
		ELEMENTARY_CHARACTER,
		GROUP,
		POINTER,
		INDEX_DATA_ITEM,
		INDEX_NAME,
		CONDITION,
		FILE,
		SORT_FILE,
		CLASS_NAME,
		OBJECT_REFERENCE,
		CURRENCY_SIGN,
		XML_SCHEMA_NAME,
		;
		
		static final Decoder<Integer, SymbolAttribute> decoder =
				new ReversibleMap<Integer, SymbolAttribute>()
				.set(0x01, NUMERIC)
				.set(0x02, ELEMENTARY_CHARACTER)
				.set(0x03, GROUP)
				.set(0x04, POINTER)
				.set(0x05, INDEX_DATA_ITEM)
				.set(0x06, INDEX_NAME)
				.set(0x07, CONDITION)
				.set(0x0F, FILE)
				.set(0x10, SORT_FILE)
				.set(0x17, CLASS_NAME)
				.set(0x18, OBJECT_REFERENCE)
				.set(0x19, CURRENCY_SIGN)
				.set(0x1A, XML_SCHEMA_NAME)
				;
		
	}
	
	/**
	 * @author 2oLDNncs 20250209
	 */
	public static enum MnemonicNameSymbolClauses {
		
		NONE,
		CSP,
		C01,
		C02,
		C03,
		C04,
		C05,
		C06,
		C07,
		C08,
		C09,
		C10,
		C11,
		C12,
		S01,
		S02,
		S03,
		S04,
		S05,
		CONSOLE,
		SYSIN_SYSIPT,
		SYSOUT_SYSLST_SYSLIST,
		SYSPUNCH_SYSPCH,
		UPSI_0,
		UPSI_1,
		UPSI_2,
		UPSI_3,
		UPSI_4,
		UPSI_5,
		UPSI_6,
		UPSI_7,
		AFP_5A,
		;
		
		static final Decoder<Integer, MnemonicNameSymbolClauses> decoder =
				new ReversibleMap<Integer, MnemonicNameSymbolClauses>()
				.set(1, CSP)
				.set(2, C01)
				.set(3, C02)
				.set(4, C03)
				.set(5, C04)
				.set(6, C05)
				.set(7, C06)
				.set(8, C07)
				.set(9, C08)
				.set(10, C09)
				.set(11, C10)
				.set(12, C11)
				.set(13, C12)
				.set(14, S01)
				.set(15, S02)
				.set(16, S03)
				.set(17, S04)
				.set(18, S05)
				.set(19, CONSOLE)
				.set(20, SYSIN_SYSIPT)
				.set(22, SYSOUT_SYSLST_SYSLIST)
				.set(24, SYSPUNCH_SYSPCH)
				.set(26, UPSI_0)
				.set(27, UPSI_1)
				.set(28, UPSI_2)
				.set(29, UPSI_3)
				.set(30, UPSI_4)
				.set(31, UPSI_5)
				.set(32, UPSI_6)
				.set(33, UPSI_7)
				.set(34, AFP_5A)
				;
		
	}
	
	/**
	 * @author 2oLDNncs 20250209
	 */
	public static enum UsageClause {
		
		USAGE_IS_DISPLAY,
		USAGE_IS_COMP_1,
		USAGE_IS_COMP_2,
		USAGE_IS_PACKED_DECIMAL_or_COMP_3,
		USAGE_IS_BINARY_etc, // COMP, COMP-4, COMP-5
		USAGE_IS_DISPLAY_1,
		USAGE_IS_POINTER,
		USAGE_IS_INDEX,
		USAGE_IS_PROCEDURE_POINTER,
		USAGE_IS_OBJECT_REFERENCE,
		FUNCTION_POINTER,
		NATIONAL,
		;
		
		static final Decoder<Integer, UsageClause> decoder =
				new ReversibleMap<Integer, UsageClause>()
				.set(0x00, USAGE_IS_DISPLAY)
				.set(0x01, USAGE_IS_COMP_1)
				.set(0x02, USAGE_IS_COMP_2)
				.set(0x03, USAGE_IS_PACKED_DECIMAL_or_COMP_3)
				.set(0x04, USAGE_IS_BINARY_etc)
				.set(0x05, USAGE_IS_DISPLAY_1)
				.set(0x06, USAGE_IS_POINTER)
				.set(0x07, USAGE_IS_INDEX)
				.set(0x08, USAGE_IS_PROCEDURE_POINTER)
				.set(0x09, USAGE_IS_OBJECT_REFERENCE)
				.set(0x0A, FUNCTION_POINTER)
				.set(0x0B, NATIONAL)
				;
	}
	
	/**
	 * @author 2oLDNncs 20250209
	 */
	public static enum SignClause {
		
		NO_SIGN,
		SIGN_IS_LEADING,
		SIGN_IS_LEADING_SEPARATE_CHARACTER,
		SIGN_IS_TRAILING,
		SIGN_IS_TRAILING_SEPARATE_CHARACTER,
		;
		
		static final Decoder<Integer, SignClause> decoder =
				new ReversibleMap<Integer, SignClause>()
				.set(0x00, NO_SIGN)
				.set(0x01, SIGN_IS_LEADING)
				.set(0x02, SIGN_IS_LEADING_SEPARATE_CHARACTER)
				.set(0x03, SIGN_IS_TRAILING)
				.set(0x04, SIGN_IS_TRAILING_SEPARATE_CHARACTER)
				;
	}
	
	/**
	 * @author 2oLDNncs 20250209
	 */
	public static enum StorageType {
		
		NOT_APPLICABLE,
		FILES,
		WORKING_STORAGE,
		LINKAGE_SECTION,
		SPECIAL_REGISTERS,
		INDEXED_BY_VARIABLE,
		UPSI_SWITCH,
		VARIABLY_LOCATED_ITEMS,
		EXTERNAL_DATA,
		ALPHANUMERIC_FUNC,
		ALPHANUMERIC_EVAL,
		OBJECT_DATA,
		LOCAL_STORAGE,
		FACTORY_DATA,
		XML_TEXT_and_XML_NTEXT,
		;
		
		static final Decoder<Integer, StorageType> decoder =
				new ReversibleMap<Integer, StorageType>()
				.set(0, NOT_APPLICABLE)
				.set(1, FILES)
				.set(2, WORKING_STORAGE)
				.set(3, LINKAGE_SECTION)
				.set(5, SPECIAL_REGISTERS)
				.set(7, INDEXED_BY_VARIABLE)
				.set(10, UPSI_SWITCH)
				.set(13, VARIABLY_LOCATED_ITEMS)
				.set(14, EXTERNAL_DATA)
				.set(15, ALPHANUMERIC_FUNC)
				.set(16, ALPHANUMERIC_EVAL)
				.set(17, OBJECT_DATA)
				.set(19, LOCAL_STORAGE)
				.set(20, FACTORY_DATA)
				.set(21, XML_TEXT_and_XML_NTEXT)
				;
	}
	
	/**
	 * @author 2oLDNncs 20250211
	 */
	public static enum KeySequence {
		
		ASCENDING,
		DESCENDING,
		;
		
		static final Decoder<Integer, KeySequence> decoder =
				new ReversibleMap<Integer, KeySequence>()
				.set(0x00, ASCENDING)
				.set(0x01, DESCENDING)
				;
	}
	
	/**
	 * @author 2oLDNncs 20250225
	 */
	public static enum DateFormat {
		
		NONE,
		YY,
		YYXX,
		YYXXXX,
		YYXXX,
		YYYY,
		YYYYXX,
		YYYYXXXX,
		YYYYXXX,
		YYX,
		YYYYX,
		XXYY,
		XXXXYY,
		XXXYY,
		XXYYYY,
		XXXXYYYY,
		XXXYYYY,
		XYY,
		XYYYY,
		;
		
		static final Decoder<Integer, DateFormat> decoder =
				new ReversibleMap<Integer, DateFormat>()
				.set( 0, NONE)
				.set( 1, YY)
				.set( 2, YYXX)
				.set( 3, YYXXXX)
				.set( 4, YYXXX)
				.set( 5, YYYY)
				.set( 6, YYYYXX)
				.set( 7, YYYYXXXX)
				.set( 8, YYYYXXX)
				.set( 9, YYX)
				.set(10, YYYYX)
				.set(22, XXYY)
				.set(23, XXXXYY)
				.set(24, XXXYY)
				.set(26, XXYYYY)
				.set(27, XXXXYYYY)
				.set(28, XXXYYYY)
				.set(29, XYY)
				.set(30, XYYYY)
				;
	}

}