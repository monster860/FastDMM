// directions
var/const
	NORTH = 1
	SOUTH = 2
	EAST = 4
	WEST = 8
	NORTHEAST = 5
	NORTHWEST = 9
	SOUTHEAST = 6
	SOUTHWEST = 10
	UP = 16
	DOWN = 32
// eye and sight
var/const
	BLIND = 1
	SEE_MOBS = 4
	SEE_OBJS = 8
	SEE_TURFS = 16
	SEE_SELF = 32
	SEE_INFRA = 64
	SEE_PIXELS = 256
	SEE_THRU = 512
	SEE_BLACKNESS = 1024
#define SEEINVIS 2
#define SEEMOBS 4
#define SEEOBJS 8
#define SEETURFS 16
var/const
	MOB_PERSPECTIVE = 0
	EYE_PERSPECTIVE = 1
	EDGE_PERSPECTIVE = 2
// layers
var/const
	FLOAT_LAYER = -1
	AREA_LAYER = 1
	TURF_LAYER = 2
	OBJ_LAYER = 3
	MOB_LAYER = 4
	FLY_LAYER = 5
	EFFECTS_LAYER = 5000
	TOPDOWN_LAYER = 10000
	BACKGROUND_LAYER = 20000
	FLOAT_PLANE = -32767
// map formats
var/const
	TOPDOWN_MAP = 0
	ISOMETRIC_MAP = 1
	SIDE_MAP = 2
	TILED_ICON_MAP = 32768
// gliding
#define NO_STEPS 0
#define FORWARD_STEPS 1
#define SLIDE_STEPS 2
#define SYNC_STEPS 3
// appearance_flags
#define LONG_GLIDE 1
#define RESET_COLOR 2
#define RESET_ALPHA 4
#define RESET_TRANSFORM 8
#define NO_CLIENT_COLOR 16
#define KEEP_TOGETHER 32
#define KEEP_APART 64
#define PLANE_MASTER 128
#define TILE_BOUND 256
var/const
	TRUE = 1
	FALSE = 0
var/const
	MALE = "male"
	FEMALE = "female"
	NEUTER = "neuter"
	PLURAL = "plural"
var/const
	MOUSE_INACTIVE_POINTER = 0
	MOUSE_ACTIVE_POINTER = 1
	MOUSE_DRAG_POINTER = 3
	MOUSE_DROP_POINTER = 4
	MOUSE_ARROW_POINTER = 5
	MOUSE_CROSSHAIRS_POINTER = 6
	MOUSE_HAND_POINTER = 7
var/const
	MOUSE_LEFT_BUTTON = 1
	MOUSE_RIGHT_BUTTON = 2
	MOUSE_MIDDLE_BUTTON = 4
	MOUSE_CTRL_KEY = 8
	MOUSE_SHIFT_KEY = 16
	MOUSE_ALT_KEY = 32
#define CONTROL_FREAK_ALL 1
#define CONTROL_FREAK_SKIN 2
#define CONTROL_FREAK_MACROS 4
var/const
	MS_WINDOWS = "MS Windows"
	UNIX = "UNIX"
#define ASSERT(c) if(!(c)) {CRASH("[__FILE__]:[__LINE__]:Assertion Failed: [#c]"); }
#define _DM_datum 0x001
#define _DM_atom 0x002
#define _DM_movable 0x004
#define _DM_sound 0x020
#define _DM_image 0x040
#define _DM_Icon 0x100
#define _DM_RscFile 0x200
#define _DM_Matrix 0x400
#define _DM_Database 0x1000
#define _DM_Regex 0x2000
#define _DM_Special 0x4000

// sound
var/const
	SOUND_MUTE = 1
	SOUND_PAUSED = 2
	SOUND_STREAM = 4
	SOUND_UPDATE = 16
sound
	var
		file
		repeat
		wait
		channel
		frequency = 0
		pan = 0
		volume = 100
		priority = 0
		status = 0
		environment = -1
		echo
		x = 0; y = 0; z = 0
		falloff = 1
	_dm_interface = _DM_datum|_DM_sound|_DM_RscFile
	New(file,repeat,wait,channel,volume=100)
		src.file = istype(file,/list) ? file : fcopy_rsc(file)
		src.repeat = repeat
		src.wait = wait
		src.channel = channel
		src.volume = volume
		return ..()
	proc
		RscFile()
			return file

// icons
#define ICON_ADD 0
#define ICON_SUBTRACT 1
#define ICON_MULTIPLY 2
#define ICON_OVERLAY 3
#define ICON_AND 4
#define ICON_OR 5
#define ICON_UNDERLAY 6
icon
	_dm_interface = _DM_datum|_DM_Icon|_DM_RscFile
	var/icon
	New(icon,icon_state,dir,frame,moving)
		src.icon = _dm_new_icon(icon,icon_state,dir,frame,moving)
	proc
		Icon()
			return icon
		RscFile()
			return fcopy_rsc(icon)
		IconStates(mode=0)
			return icon_states(icon,mode)
		Turn(angle,antialias)
			if(antialias) _dm_turn_icon(icon,angle,1)
			else _dm_turn_icon(icon,angle)
		Flip(dir)
			_dm_flip_icon(icon,dir)
		Shift(dir,offset,wrap)
			_dm_shift_icon(icon,dir,offset,wrap)
		SetIntensity(r,g=-1,b=-1)
			_dm_icon_intensity(icon,r,g,b)
		Blend(icon,f,x=1,y=1)
			_dm_icon_blend(src.icon,icon,f,x,y)
		SwapColor(o,n)
			_dm_icon_swap_color(icon,o,n)
		DrawBox(c,x1,y1,x2,y2)
			_dm_icon_draw_box(icon,c,x1,y1,x2,y2)
		Insert(new_icon,icon_state,dir,frame,moving,delay)
			_dm_icon_insert(icon,new_icon,icon_state,dir,frame,moving,delay)
		MapColors(a,b,c,d,e,f,g,h,i,j=0,k=0,l=0)
			if(istext(a))
				if(!e) _dm_icon_map_colors(icon,a,b,c,d)
				else _dm_icon_map_colors(icon,a,b,c,d,e)
			else if(args.len <= 12) _dm_icon_map_colors(icon,a,b,c,d,e,f,g,h,i,j,k,l)
			else _dm_icon_map_colors(icon,a,b,c,d,e,f,g,h,i,j,k,l,args[13],args[14],args[15],args[16],args[17],args[18],args[19],args[20])
		Scale(x,y)
			_dm_icon_scale(icon,x,y)
		Crop(x1,y1,x2,y2)
			_dm_icon_crop(icon,x1,y1,x2,y2)
		GetPixel(x,y,icon_state,dir,frame,moving)
			return _dm_icon_getpixel(icon,x,y,icon_state,dir,frame,moving)
		Width()
			return _dm_icon_size(icon,1)
		Height()
			return _dm_icon_size(icon,2)

// matrix
#define MATRIX_COPY 0
#define MATRIX_MULTIPLY 1
#define MATRIX_ADD 2
#define MATRIX_SUBTRACT 3
#define MATRIX_INVERT 4
#define MATRIX_ROTATE 5
#define MATRIX_SCALE 6
#define MATRIX_TRANSLATE 7
#define MATRIX_INTERPOLATE 8
#define MATRIX_MODIFY 128
matrix
	var/a=1,b=0,c=0,d=0,e=1,f=0
	_dm_interface = _DM_datum|_DM_Matrix
	New(m)
		if(args.len == 6)
			a = m; b = args[2]; c = args[3]; src.d = args[4]; src.e = args[5]; src.f = args[6]
		else if(m) matrix(src,m,MATRIX_COPY|MATRIX_MODIFY)
	proc
		Multiply(m) return matrix(src,m,MATRIX_MULTIPLY|MATRIX_MODIFY)
		Add(m) return matrix(src,m,MATRIX_ADD|MATRIX_MODIFY)
		Subtract(m) return matrix(src,m,MATRIX_SUBTRACT|MATRIX_MODIFY)
		Invert() return matrix(src,MATRIX_INVERT|MATRIX_MODIFY)
		Turn(a) return matrix(src,a,MATRIX_ROTATE|MATRIX_MODIFY)
		Scale(x,y)
			if(isnull(y)) y = x
			return matrix(src,x,y,MATRIX_SCALE|MATRIX_MODIFY)
		Translate(x,y)
			if(isnull(y)) y = x
			return matrix(src,x,y,MATRIX_TRANSLATE|MATRIX_MODIFY)
		Interpolate(m2,t)
			return matrix(src,m2,t,MATRIX_INTERPOLATE)

// animation easing
#define LINEAR_EASING 0
#define SINE_EASING 1
#define CIRCULAR_EASING 2
#define CUBIC_EASING 3
#define BOUNCE_EASING 4
#define ELASTIC_EASING 5
#define BACK_EASING 6
#define QUAD_EASING 7
#define EASE_IN 64
#define EASE_OUT 128

// animation flags
#define ANIMATION_END_NOW 1
#define ANIMATION_LINEAR_TRANSFORM 2
#define ANIMATION_PARALLEL 4

// blend_mode
var/const
	BLEND_DEFAULT = 0
	BLEND_OVERLAY = 1
	BLEND_ADD = 2
	BLEND_SUBTRACT = 3
	BLEND_MULTIPLY = 4

// Database
#define DATABASE_OPEN 0
#define DATABASE_CLOSE 1
#define DATABASE_ERROR_CODE 2
#define DATABASE_ERROR 3
#define DATABASE_QUERY_CLEAR 4
#define DATABASE_QUERY_ADD 5
#define DATABASE_QUERY_EXEC 8
#define DATABASE_QUERY_NEXT 9
#define DATABASE_QUERY_ABORT 10
#define DATABASE_QUERY_RESET 11
#define DATABASE_QUERY_ROWS_AFFECTED 12
#define DATABASE_ROW_COLUMN_NAMES 16
#define DATABASE_ROW_COLUMN_VALUE 17
#define DATABASE_ROW_LIST 18
database
	_dm_interface = _DM_datum|_DM_Database
	var/_binobj
	New(filename)
		if(filename) Open(filename)
	proc/Open(filename)
		_dm_database(src, DATABASE_OPEN, filename)
	proc/Close()
		_dm_database(src, DATABASE_CLOSE)
	proc/Error()
		return _dm_database(src, DATABASE_ERROR_CODE)
	proc/ErrorMsg()
		return _dm_database(src, DATABASE_ERROR)
	query
		var/database/database
		New(query)
			_dm_database(src, DATABASE_QUERY_ADD, (args.len>1 ? args : query))
		Open()
		proc/Clear()
			_dm_database(src, DATABASE_QUERY_CLEAR)
		proc/Add(query)
			_dm_database(src, DATABASE_QUERY_ADD, (args.len>1 ? args : query))
		proc/Execute(database/database)
			if(database && !istype(database)) database = new(database)
			src.database=(database||src.database)
			return _dm_database(src, DATABASE_QUERY_EXEC, src.database)
		proc/NextRow()
			return _dm_database(src, DATABASE_QUERY_NEXT)
		proc/RowsAffected()
			return _dm_database(src, DATABASE_QUERY_ROWS_AFFECTED)
		Close()
			return _dm_database(src, DATABASE_QUERY_ABORT)
		proc/Reset()
			return _dm_database(src, DATABASE_QUERY_RESET)
		proc/Columns(column)
			return _dm_database(src, DATABASE_ROW_COLUMN_NAMES, (isnum(column) ? column-1 : column))
		proc/GetColumn(column)
			return _dm_database(src, DATABASE_ROW_COLUMN_VALUE, column-1)
		proc/GetRowData()
			return _dm_database(src, DATABASE_ROW_LIST)

exception
	var/name,desc,file,line
	New(name,file,line)	// not called by regular crashes
		src.name=name
		src.file=file
		src.line=line
#define EXCEPTION(m) new/exception((m),__FILE__,__LINE__)

#define REGEX_QUOTE(a) regex((a), 1)
#define REGEX_QUOTE_REPLACEMENT(a) regex((a), 2)
regex
	_dm_interface = _DM_datum|_DM_Regex
	var/name
	var/flags
	var/_binobj
	var/text	// last text checked
	var/match	// last match
	var/group	// list of groups in last match
	var/index	// index of last match
	var/next	// index to search next
	New(text, flags)
		regex(src, text, flags)
	proc/Find(text, start=1, end)
		return findtext(text, src, start, end)
	proc/Replace(text, rep, start=1, end)
		return replacetext(text, src, rep, start, end)

mutable_appearance
	parent_type = /image
	_dm_interface = _DM_datum|_DM_image|_DM_Special
