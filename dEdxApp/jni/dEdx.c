#include <string.h>
#include <jni.h>
#include <android/log.h>
#include <libdedx/dedx.h>
#include <libdedx/dedx_tools.h>

/*
 * Helper functions
 */

// global variable
jclass dedxIdxNameClass;
jclass linkedListClass;

jobject
create_dedxIdxName(JNIEnv* env, jint idx, jstring name)
{
	// Get the DedxIdxName class
	jclass dedxIdxNameClass = (*env)->FindClass(env, "dk/au/aptg/dEdx/DedxIdxName");
	// Check if we properly got the long array class
	if (dedxIdxNameClass == NULL)
	{
		__android_log_print(ANDROID_LOG_INFO, "libdedx", "Not found DedxIdxName class");
		return NULL;
	}

	// Get the DedxIdxName constructor
	jmethodID dedxIdxNameInit =  (*env)->GetMethodID(env, dedxIdxNameClass, "<init>", "(ILjava/lang/String;)V");
	if (dedxIdxNameInit == NULL)
	{
		__android_log_print(ANDROID_LOG_INFO, "libdedx", "Not found DedxIdxName constructor");
		return NULL;
	}

	// Create the DedxIdxName object
	jobject dedxIdxNameObj = (*env)->NewObject(env, dedxIdxNameClass, dedxIdxNameInit, idx, name);
	if (dedxIdxNameObj == NULL)
	{
		__android_log_print(ANDROID_LOG_INFO, "libdedx", "Not found DedxIdxName object");
		return NULL;
	}
	(*env)->DeleteLocalRef(env, dedxIdxNameClass);
	return dedxIdxNameObj;
}


jobject
create_linkedlist(JNIEnv* env)
{
	// Get the LinkedList class
	linkedListClass = (*env)->FindClass(env, "java/util/LinkedList");
	if (linkedListClass == NULL)
	{
		__android_log_print(ANDROID_LOG_INFO, "libdedx", "Did not found LinkedList class");
		return NULL;
	}

	// Get the LinkedList constructor
	jmethodID linkedListInit =  (*env)->GetMethodID(env, linkedListClass, "<init>", "()V");
	if (linkedListInit == NULL)
	{
		__android_log_print(ANDROID_LOG_INFO, "libdedx", "Did not found LinkedList constructor");
		return NULL;
	}

	// Create the DedxIdxName object
	jobject linkedListObj = (*env)->NewObject(env, linkedListClass, linkedListInit);
	if (linkedListObj == NULL)
	{
		__android_log_print(ANDROID_LOG_INFO, "libdedx", "Did not found LinkedList object");
		return NULL;
	}

	return linkedListObj;
}

void add_linkedList(JNIEnv* env, jobject linkedListObj, jobject dedxIdxNameObj)
{
	// Get the LinkedList add method
	jmethodID linkedListAdd =  (*env)->GetMethodID(env, linkedListClass, "add", "(Ljava/lang/Object;)Z");
	if (linkedListAdd == NULL)
	{
		__android_log_print(ANDROID_LOG_INFO, "libdedx", "Did not found LinkedList constructor");
	}

	// Add the DedxIdxName object
	jboolean result = (*env)->CallBooleanMethod(env, linkedListObj, linkedListAdd, dedxIdxNameObj);
	if (result == 0)
	{
		__android_log_print(ANDROID_LOG_INFO, "libdedx", "Object not added to LinkedList object");
	}
}

/*
 * API for Java -> libdedx
 */

// Global variables
dedx_workspace *ws = 0;
dedx_config *cfg = 0;
char externStoragePath[1000];

jint
JNI_OnLoad(JavaVM* vm, void* reserved) {
	return JNI_VERSION_1_6;
}

void
Java_dk_au_aptg_dEdx_DedxAPI_dedxInit(JNIEnv* env, jobject thiz, jstring path) {
	extern char* folder;
	const char *nativePath = (*env)->GetStringUTFChars(env, path, 0);

	strcpy(externStoragePath, nativePath);
	folder = externStoragePath;

	__android_log_print(ANDROID_LOG_INFO, "libdedx", "init path: %s", folder);
}

jint
Java_dk_au_aptg_dEdx_DedxAPI_dedxExit(JNIEnv* env, jobject thiz) {
	int err = 0;

	__android_log_print(ANDROID_LOG_INFO, "libdedx", "exit");

	if(ws != 0) {
		dedx_free_workspace(ws, &err);
		if (err != 0)
			__android_log_print(ANDROID_LOG_INFO, "libdedx", "dedx_free_workspace with err %d", err);
		ws = 0;
	}

	if(cfg != 0) {
		dedx_free_config(cfg, &err);
		if (err != 0)
			__android_log_print(ANDROID_LOG_INFO, "libdedx", "dedx_free_config with err %d", err);
		cfg = 0;
	}

	return err;
}

jint
Java_dk_au_aptg_dEdx_DedxAPI_dedxLoadConfig(JNIEnv* env, jobject thiz, jint program, jint target, jint ion) {
	int err = 0;


	if(cfg != 0) {
		//Reset config
		dedx_free_config(cfg, &err);
		if (err != 0)
			__android_log_print(ANDROID_LOG_INFO, "libdedx", "dedx_free_config with err %d", err);
		cfg = 0;
	}

	if(ws != 0) {
		//Reset workspace
		dedx_free_workspace(ws, &err);
		if (err != 0)
			__android_log_print(ANDROID_LOG_INFO, "libdedx", "dedx_free_workspace with err %d", err);
		ws = 0;
	}

	ws = dedx_allocate_workspace(1, &err);
	if (err != 0) {
		__android_log_print(ANDROID_LOG_INFO, "libdedx", "dedx_allocate_workspace with err %d", err);
		return err;
	}

	cfg = (dedx_config *) calloc(1, sizeof(dedx_config));
	cfg->program = program;
	cfg->target = target;
	cfg->ion = ion;

	__android_log_print(ANDROID_LOG_INFO, "libdedx", "dedx_load_config: p=%d t=%d ion=%d", program, target, ion);

	dedx_load_config(ws, cfg, &err);
	if (err != 0)
		__android_log_print(ANDROID_LOG_INFO, "libdedx", "dedx_load_config with err %d", err);

	return err;
}

jfloat
Java_dk_au_aptg_dEdx_DedxAPI_dedxGetStp(JNIEnv* env, jobject thiz, jfloat energy) {
	int err = 0;
	jfloat stp = 0.0;

	__android_log_print(ANDROID_LOG_INFO, "libdedx", "dedx_get_stp energy %f", energy);
	stp = dedx_get_stp(ws, cfg, energy, &err);
	if (err != 0) {
		__android_log_print(ANDROID_LOG_INFO, "libdedx", "dedx_get_stp with err %d", err);
		return -1*err;
	}

	return stp;
}

jdouble
Java_dk_au_aptg_dEdx_DedxAPI_dedxGetInverseCSDA(JNIEnv* env, jobject thiz, jfloat range, jint ion_a) {
	int err = 0;
	jdouble energy;

	__android_log_print(ANDROID_LOG_INFO, "libdedx", "dedx_get_inverse_csda range %f and ion_a %i", range, ion_a);
	cfg->ion_a = ion_a;
	energy = dedx_get_inverse_csda(ws, cfg, range, &err);
	if (err != 0) {
		__android_log_print(ANDROID_LOG_INFO, "libdedx", "dedx_get_inverse_csda with err %d", err);
		return -1*err;
	}

	return energy;
}

jstring
Java_dk_au_aptg_dEdx_DedxAPI_dedxGetErrorMsg(JNIEnv* env, jobject thiz, jint err) {
	char errorMsg[100];

	dedx_get_error_code(&errorMsg[0], err);

	return (*env)->NewStringUTF(env, errorMsg);
}

jdouble
Java_dk_au_aptg_dEdx_DedxAPI_dedxGetCSDARange(JNIEnv* env, jobject thiz, jfloat energy, jint ion_a) {
	int err = 0;
	jdouble CSDARange = 1.0;

	__android_log_print(ANDROID_LOG_INFO, "libdedx", "dedx_get_CSDA_range energy %f and ion_a %i", energy, ion_a);
	cfg->ion_a = ion_a;
	CSDARange = dedx_get_csda(ws, cfg, energy, &err);

	if (err != 0) {
		__android_log_print(ANDROID_LOG_INFO, "libdedx", "dedx_get_CSDA_range with err %d", err);
		return -1*err;
	}

	return CSDARange;
}

jobject
Java_dk_au_aptg_dEdx_DedxAPI_dedxGetProgramList(JNIEnv* env, jobject thiz) {
	// Create the DedxIdxName object
	jobject linkedListObj = create_linkedlist(env);

	const int* p;
	for (p=dedx_get_program_list(); *p != -1; p++) {
		if(*p != DEDX_ESTAR && *p != DEDX_DEFAULT && *p != DEDX_PSTAR && *p != DEDX_DEFAULT && *p != DEDX_ASTAR && *p != DEDX_ICRU73
				&& *p != DEDX_ICRU49 && *p != DEDX_ICRU73_OLD) {
			if(*p == DEDX_ICRU)
				add_linkedList(env, linkedListObj, create_dedxIdxName(env, *p, (*env)->NewStringUTF(env, "ICRU 49 & 73")));
			else
				add_linkedList(env, linkedListObj, create_dedxIdxName(env, *p, (*env)->NewStringUTF(env, dedx_get_program_name(*p))));
		}
	}
	return linkedListObj;
}

jobject
Java_dk_au_aptg_dEdx_DedxAPI_dedxGetIons(JNIEnv* env, jobject thiz, jint program) {
	// Create the DedxIdxName object
	jobject linkedListObj = create_linkedlist(env);

	const int* p;
	for (p=dedx_get_ion_list(program); *p != -1; p++)
	{
		jstring name = (*env)->NewStringUTF(env, dedx_get_ion_name(*p));
		add_linkedList(env, linkedListObj, create_dedxIdxName(env, *p, name));
		(*env)->DeleteLocalRef(env, name);

	}

	return linkedListObj;
}

jobject
Java_dk_au_aptg_dEdx_DedxAPI_dedxGetMaterials(JNIEnv* env, jobject thiz, jint program) {
	// Create the DedxIdxName object
	jobject linkedListObj = create_linkedlist(env);

	const int* p;
	for (p=dedx_get_material_list(program); *p != -1; p++)
	{
		jstring name = (*env)->NewStringUTF(env, dedx_get_material_name(*p));
		add_linkedList(env, linkedListObj, create_dedxIdxName(env, *p, name));
		(*env)->DeleteLocalRef(env, name);
	}

	return linkedListObj;
}
