package t6bygedq.lib.cbl;

import t6bygedq.lib.Helpers;

/**
 * @author 2oLDNncs 20250202
 */
public final class RecData_X0010_Options_4_2 extends RecData {
	
	public final BooleanVar vDeck                                 = this.newBooleanVar(BYTE_1, 0);
	public final BooleanVar vAdata                                = this.newBooleanVar(BYTE_1, 1);
	public final BooleanVar vCollseqEbcdic                        = this.newBooleanVar(BYTE_1, 2);
	public final BooleanVar vSepobj                               = this.newBooleanVar(BYTE_1, 3);
	public final BooleanVar vName                                 = this.newBooleanVar(BYTE_1, 4);
	public final BooleanVar vObject                               = this.newBooleanVar(BYTE_1, 5);
	public final BooleanVar vSql                                  = this.newBooleanVar(BYTE_1, 6);
	public final BooleanVar vCics                                 = this.newBooleanVar(BYTE_1, 7);
	public final BooleanVar vOffset                               = this.newBooleanVar(BYTE_2, 0);
	public final BooleanVar vMap                                  = this.newBooleanVar(BYTE_2, 1);
	public final BooleanVar vList                                 = this.newBooleanVar(BYTE_2, 2);
	public final BooleanVar vDbcsxref                             = this.newBooleanVar(BYTE_2, 3);
	public final BooleanVar vXrefShort                            = this.newBooleanVar(BYTE_2, 4);
	public final BooleanVar vSource                               = this.newBooleanVar(BYTE_2, 5);
	public final BooleanVar vVbref                                = this.newBooleanVar(BYTE_2, 6);
	public final BooleanVar vXref                                 = this.newBooleanVar(BYTE_2, 7);
	public final BooleanVar vFlagImbeddedDiagnosticLevelSpecified = this.newBooleanVar(BYTE_3, 0);
	public final BooleanVar vFlagstd                              = this.newBooleanVar(BYTE_3, 1);
	public final BooleanVar vNum                                  = this.newBooleanVar(BYTE_3, 2);
	public final BooleanVar vSequence                             = this.newBooleanVar(BYTE_3, 3);
	public final BooleanVar vSosi                                 = this.newBooleanVar(BYTE_3, 4);
	public final BooleanVar vNsymbolNational                      = this.newBooleanVar(BYTE_3, 5);
	public final BooleanVar vProfile                              = this.newBooleanVar(BYTE_3, 6);
	public final BooleanVar vWord                                 = this.newBooleanVar(BYTE_3, 7);
	public final BooleanVar vAdv                                  = this.newBooleanVar(BYTE_4, 0);
	public final BooleanVar vApost                                = this.newBooleanVar(BYTE_4, 1);
	public final BooleanVar vDynam                                = this.newBooleanVar(BYTE_4, 2);
	public final BooleanVar vAwo                                  = this.newBooleanVar(BYTE_4, 3);
	public final BooleanVar vRmodeSpecified                       = this.newBooleanVar(BYTE_4, 4);
	public final BooleanVar vRent                                 = this.newBooleanVar(BYTE_4, 5);
	public final BooleanVar vRes                                  = this.newBooleanVar(BYTE_4, 6);
	public final BooleanVar vRmode24                              = this.newBooleanVar(BYTE_4, 7);
	public final BooleanVar vSqlccsid                             = this.newBooleanVar(BYTE_5, 0);
	public final BooleanVar vOpt12                                = this.newBooleanVar(BYTE_5, 1);
	public final BooleanVar vSqlims                               = this.newBooleanVar(BYTE_5, 2);
	public final BooleanVar vDbcs                                 = this.newBooleanVar(BYTE_5, 3);
	public final BooleanVar vAfpVolatile                          = this.newBooleanVar(BYTE_5, 4);
	public final BooleanVar vSsrange                              = this.newBooleanVar(BYTE_5, 5);
	public final BooleanVar vTest                                 = this.newBooleanVar(BYTE_5, 6);
	public final BooleanVar vProbe                                = this.newBooleanVar(BYTE_5, 7);
	
	public final BooleanVar vNumprocPfd                           = this.newBooleanVar(BYTE_6, 2);
	public final BooleanVar vNumclsAlt                            = this.newBooleanVar(BYTE_6, 3);
	public final BooleanVar vBinaryS390                           = this.newBooleanVar(BYTE_6, 5);
	public final BooleanVar vTruncStd                             = this.newBooleanVar(BYTE_6, 6);
	public final BooleanVar vZwb                                  = this.newBooleanVar(BYTE_6, 7);
	
	public final BooleanVar vAlowcbl                              = this.newBooleanVar(BYTE_7, 0);
	public final BooleanVar vTerm                                 = this.newBooleanVar(BYTE_7, 1);
	public final BooleanVar vDump                                 = this.newBooleanVar(BYTE_7, 2);
	public final BooleanVar vCurrency                             = this.newBooleanVar(BYTE_7, 6);
	
	public final BooleanVar vRules                                = this.newBooleanVar(BYTE_8, 0);
	public final BooleanVar vOptfile                              = this.newBooleanVar(BYTE_8, 1);
	public final BooleanVar vAddr64                               = this.newBooleanVar(BYTE_8, 2);
	public final BooleanVar vBlock0                               = this.newBooleanVar(BYTE_8, 4);
	
	public final BooleanVar vData24                               = this.newBooleanVar(BYTE_9, 0);
	public final BooleanVar vFastsrt                              = this.newBooleanVar(BYTE_9, 1);
	public final BooleanVar vSizeMax                              = this.newBooleanVar(BYTE_9, 2);
	public final BooleanVar vThread                               = this.newBooleanVar(BYTE_9, 5);
	
	public final BooleanVar vNcollseqLocale                       = this.newBooleanVar(BYTE_C, 0);
	public final BooleanVar vIntdateLilian                        = this.newBooleanVar(BYTE_C, 2);
	public final BooleanVar vNcollseqBinary                       = this.newBooleanVar(BYTE_C, 3);
	public final BooleanVar vCharEbcdic                           = this.newBooleanVar(BYTE_C, 4);
	public final BooleanVar vFloatHex                             = this.newBooleanVar(BYTE_C, 5);
	public final BooleanVar vCollseqBinary                        = this.newBooleanVar(BYTE_C, 6);
	public final BooleanVar vCollseqLocale                        = this.newBooleanVar(BYTE_C, 7);
	
	public final BooleanVar vDll                                  = this.newBooleanVar(BYTE_D, 0);
	public final BooleanVar vExportall                            = this.newBooleanVar(BYTE_D, 1);
	public final BooleanVar vCodepage                             = this.newBooleanVar(BYTE_D, 2);
	public final BooleanVar vDateProc                             = this.newBooleanVar(BYTE_D, 3);
	public final BooleanVar vDateProcFlag                         = this.newBooleanVar(BYTE_D, 4);
	public final BooleanVar vYearWindow                           = this.newBooleanVar(BYTE_D, 5);
	public final BooleanVar vWsclear                              = this.newBooleanVar(BYTE_D, 6);
	public final BooleanVar vBeopt                                = this.newBooleanVar(BYTE_D, 7);
	
	public final BooleanVar vDateProcTrig                         = this.newBooleanVar(BYTE_E, 0);
	public final BooleanVar vDiagtrunc                            = this.newBooleanVar(BYTE_E, 1);
	public final BooleanVar vLstfileUtf8                          = this.newBooleanVar(BYTE_E, 5);
	public final BooleanVar vMdec                                 = this.newBooleanVar(BYTE_E, 6);
	public final BooleanVar vMdeckNocompile                       = this.newBooleanVar(BYTE_E, 7);
	
	public final BooleanVar vMinimum                              = this.newBooleanVar(FIPS_FLAGSTD, 0);
	public final BooleanVar vIntermediate                         = this.newBooleanVar(FIPS_FLAGSTD, 1);
	public final BooleanVar vHigh                                 = this.newBooleanVar(FIPS_FLAGSTD, 2);
	public final BooleanVar vIbmExtension                         = this.newBooleanVar(FIPS_FLAGSTD, 3);
	public final BooleanVar vLevel1Segmentation                   = this.newBooleanVar(FIPS_FLAGSTD, 4);
	public final BooleanVar vLevel2Segmentation                   = this.newBooleanVar(FIPS_FLAGSTD, 5);
	public final BooleanVar vDebugging                            = this.newBooleanVar(FIPS_FLAGSTD, 6);
	public final BooleanVar vObsolete                             = this.newBooleanVar(FIPS_FLAGSTD, 7);
	
	public final BooleanVar vNameAlias                            = this.newBooleanVar(TERN_OPTS_DATA, 0);
	public final BooleanVar vNumProcMig                           = this.newBooleanVar(TERN_OPTS_DATA, 1);
	public final BooleanVar vTruncBin                             = this.newBooleanVar(TERN_OPTS_DATA, 2);
	
	public final BooleanVar vTestHook                             = this.newBooleanVar(TEST_SUBOPTS, 0);
	public final BooleanVar vTestSep                              = this.newBooleanVar(TEST_SUBOPTS, 1);
	public final BooleanVar vTestEjpd                             = this.newBooleanVar(TEST_SUBOPTS, 2);
	
	public final BooleanVar vPgmnameCompat                        = this.newBooleanVar(PGMNAME_SUBOPTS, 0);
	public final BooleanVar vPgmnameLongupper                     = this.newBooleanVar(PGMNAME_SUBOPTS, 1);
	public final BooleanVar vPgmnameLongmixed                     = this.newBooleanVar(PGMNAME_SUBOPTS, 2);
	
	public final BooleanVar vEntryInterfaceSystem                 = this.newBooleanVar(ENTRY_INTERFACE_SUBOPTS, 0);
	public final BooleanVar vEntryInterfaceOptLink                = this.newBooleanVar(ENTRY_INTERFACE_SUBOPTS, 1);
	
	public final BooleanVar vCallinterfaceSystem                  = this.newBooleanVar(CALLINTERFACE_SUBOPTS, 0);
	public final BooleanVar vCallinterfaceOptLink                 = this.newBooleanVar(CALLINTERFACE_SUBOPTS, 1);
	public final BooleanVar vCallinterfaceCdecl                   = this.newBooleanVar(CALLINTERFACE_SUBOPTS, 3);
	public final BooleanVar vCallinterfaceSystemDesc              = this.newBooleanVar(CALLINTERFACE_SUBOPTS, 4);
	
	public final BooleanVar vArithCompat                          = this.newBooleanVar(ARITH_SUBOPTS, 0);
	public final BooleanVar vArithExtend                          = this.newBooleanVar(ARITH_SUBOPTS, 1);
	
	public final BooleanVar vIgyclibrInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_1, 0);
	public final BooleanVar vIgycscanInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_1, 1);
	public final BooleanVar vIgycdscnInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_1, 2);
	public final BooleanVar vIgycgrouInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_1, 3);
	public final BooleanVar vIgycpscnInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_1, 4);
	public final BooleanVar vIgycpanaInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_1, 5);
	public final BooleanVar vIgycfgenInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_1, 6);
	public final BooleanVar vIgycpgenInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_1, 7);
	
	public final BooleanVar vIgycoptmInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_2, 0);
	public final BooleanVar vIgyclstrInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_2, 1);
	public final BooleanVar vIgycxrefInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_2, 2);
	public final BooleanVar vIgycdmapInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_2, 3);
	public final BooleanVar vIgycasm1InUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_2, 4);
	public final BooleanVar vIgycasm2InUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_2, 5);
	public final BooleanVar vIgycdiagInUserRegion                 = this.newBooleanVar(PHASE_RESIDENCE_BYTE_2, 6);
	
	public final EnumVar<FlagLevel>    vFlagLevel                 = this.newEnumVar(FLAG_LEVEL,     FlagLevel.decoder);
	public final EnumVar<FlagLevel>    vImbeddedDiagnosticLevel   = this.newEnumVar(IMB_DIAG_LEVEL, FlagLevel.decoder);
	public final EnumVar<CompilerMode> vCompilerMode              = this.newEnumVar(COMPILER_MODE,  CompilerMode.decoder);
	
	public final StringVar  vLevelInfo                            = this.newStringVarF(LEVEL_INFO);
	public final LongVar    vDbcsReq                              = this.newLongVar(DBCS_REQ);
	public final StringVar  vDbcsOrdType                          = this.newStringVarF(DBCS_ORD_TYPE);
	public final StringVar  vConvertedSo                          = this.newStringVarF(CONVERTED_SO);
	public final StringVar  vConvertedSi                          = this.newStringVarF(CONVERTED_SI);
	public final StringVar  vLanguageId                           = this.newStringVarF(LANGUAGE_ID);
	public final StringVar  vCurropt                              = this.newStringVarF(CURROPT);
	public final IntVar     vYearwindow                           = this.newIntVar(YEARWINDOW);
	public final IntVar     vCodepageValue                        = this.newIntVar(CODEPAGE);
	public final IntVar     vLinecnt                              = this.newIntVar(LINECNT);
	public final LongVar    vBufsize                              = this.newLongVar(BUFSIZE);
	public final LongVar    vSize                                 = this.newLongVar(SIZE);
	
	public final StringVar  vOutddName                            = this.newStringVarV(OUTDD_NAME_LENGTH);
	public final StringVar  vRwtId                                = this.newStringVarV(RWT_ID_LENGTH);
	public final StringVar  vDbcsOrdpgm                           = this.newStringVarV(DBCS_ORDPGM_LENGTH);
	public final StringVar  vDbcsEnctbl                           = this.newStringVarV(DBCS_ENCTBL_LENGTH);
	public final StringVar  vInexitName                           = this.newStringVarV(INEXIT_NAME_LENGTH);
	public final StringVar  vPrtexitName                          = this.newStringVarV(PRTEXIT_NAME_LENGTH);
	public final StringVar  vLibexitName                          = this.newStringVarV(LIBEXIT_NAME_LENGTH);
	public final StringVar  vAdexitName                           = this.newStringVarV(ADEXIT_NAME_LENGTH);
	
	public RecData_X0010_Options_4_2(final Buffer buffer) {
		super(buffer);
		this.buffer.setNum(SPACE_01, Buffer.SPACE);
	}
	
	public final boolean isXrefFull() {
		return !this.vXrefShort.get() && this.vXref.get();
	}
	
	private static final Buffer.Region.Generator staticRegionGenerator =
			getStaticRegionGenerator(RecData_X0010_Options_4_2.class);
	
	private static final Buffer.Region BYTE_0                  = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region BYTE_1                  = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region BYTE_2                  = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region BYTE_3                  = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region BYTE_4                  = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region BYTE_5                  = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region BYTE_6                  = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region BYTE_7                  = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region BYTE_8                  = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region BYTE_9                  = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region BYTE_A                  = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region BYTE_B                  = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region BYTE_C                  = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region BYTE_D                  = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region BYTE_E                  = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region BYTE_F                  = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region FLAG_LEVEL              = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region IMB_DIAG_LEVEL          = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region FIPS_FLAGSTD            = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region RESERVED_01             = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region COMPILER_MODE           = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region SPACE_01                = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region TERN_OPTS_DATA          = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region TEST_SUBOPTS            = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region OUTDD_NAME_LENGTH       = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region RWT_ID_LENGTH           = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region LEVEL_INFO              = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region PGMNAME_SUBOPTS         = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region ENTRY_INTERFACE_SUBOPTS = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region CALLINTERFACE_SUBOPTS   = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region ARITH_SUBOPTS           = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region DBCS_REQ                = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region DBCS_ORDPGM_LENGTH      = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region DBCS_ENCTBL_LENGTH      = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region DBCS_ORD_TYPE           = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region RESERVED_02             = staticRegionGenerator.newFixedLength(6);
	private static final Buffer.Region CONVERTED_SO            = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region CONVERTED_SI            = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region LANGUAGE_ID             = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region RESERVED_03             = staticRegionGenerator.newFixedLength(8);
	private static final Buffer.Region INEXIT_NAME_LENGTH      = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region PRTEXIT_NAME_LENGTH     = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region LIBEXIT_NAME_LENGTH     = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region ADEXIT_NAME_LENGTH      = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region CURROPT                 = staticRegionGenerator.newFixedLength(5);
	private static final Buffer.Region RESERVED_04             = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region YEARWINDOW              = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region CODEPAGE                = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region RESERVED_05             = staticRegionGenerator.newFixedLength(50);
	private static final Buffer.Region LINECNT                 = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region RESERVED_06             = staticRegionGenerator.newFixedLength(2);
	private static final Buffer.Region BUFSIZE                 = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region SIZE                    = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region RESERVED_07             = staticRegionGenerator.newFixedLength(4);
	private static final Buffer.Region PHASE_RESIDENCE_BYTE_1  = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region PHASE_RESIDENCE_BYTE_2  = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region PHASE_RESIDENCE_BYTE_3  = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region PHASE_RESIDENCE_BYTE_4  = staticRegionGenerator.newFixedLength(1);
	private static final Buffer.Region RESERVED_08             = staticRegionGenerator.newFixedLength(8);
	
	static {
		Helpers.ignore(BYTE_0);
		Helpers.ignore(BYTE_A);
		Helpers.ignore(BYTE_B);
		Helpers.ignore(BYTE_F);
		Helpers.ignore(RESERVED_01);
		Helpers.ignore(RESERVED_02);
		Helpers.ignore(RESERVED_03);
		Helpers.ignore(RESERVED_04);
		Helpers.ignore(RESERVED_05);
		Helpers.ignore(RESERVED_06);
		Helpers.ignore(RESERVED_07);
		Helpers.ignore(PHASE_RESIDENCE_BYTE_3);
		Helpers.ignore(PHASE_RESIDENCE_BYTE_4);
		Helpers.ignore(RESERVED_08);
	}
	
	/**
	 * @author 2oLDNncs 20250208
	 */
	public static enum FlagLevel {
		
		FLAG_I, FLAG_W, FLAG_E, FLAG_S, FLAG_U, NOFLAG;
		
		static final Decoder<Integer, FlagLevel> decoder =
				new ReversibleMap<Integer, FlagLevel>()
				.set(0x00, FLAG_I)
				.set(0x04, FLAG_W)
				.set(0x08, FLAG_E)
				.set(0x0C, FLAG_S)
				.set(0x10, FLAG_U)
				.set(0xFF, NOFLAG)
				;
		
	}
	
	/**
	 * @author 2oLDNncs 20250208
	 */
	public static enum CompilerMode {
		
		NOCOMPILE_I, NOCOMPILE_W, NOCOMPILE_E, NOCOMPILE_S, COMPILE;
		
		static final Decoder<Integer, CompilerMode> decoder =
				new ReversibleMap<Integer, CompilerMode>()
				.set(0x00, NOCOMPILE_I)
				.set(0x04, NOCOMPILE_W)
				.set(0x08, NOCOMPILE_E)
				.set(0x0C, NOCOMPILE_S)
				.set(0xFF, COMPILE)
				;
		
	}
	
}