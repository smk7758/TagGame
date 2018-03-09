package com.github.smk7758.TagGame.Files;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.github.smk7758.TagGame.Util.SendLog;

public class YamlFileManager {

	private YamlFileManager() {
	}

	public static YamlFile reloadYamlFile(YamlFile file) {
		file.reloadFileConfiguration();
		return loadFields(file, file, null);
	}

	/**
	 * YamlFileにfields
	 *
	 * @param file_object 保存するYamlFileのObject。
	 * @param parent 取得するFieldを持ったObject?
	 * @param parent_yaml_path YamlFileの保存する親のパス。親パスが無い時はnull。
	 */
	private static YamlFile loadFields(YamlFile file_object, Object parent, String parent_yaml_path) {
		if (file_object == null || parent == null) throw new IllegalArgumentException(
				"FileObject: " + file_object + " | Parent: " + parent);

		boolean is_root_class = false; // YamlFileのObject=rootのclassのObjectであるか。
		String yaml_path_access = ""; // for文で更新され、実際のYAMLファイルに探しに行くパス。
		Object object = null; // for文で更新され、YamlFileのフィールドに代入されるObject。
		Field[] fields = parent.getClass().getFields(); // YamlFileのフィールド。

		// is root yaml file.
		if (parent_yaml_path == null || parent_yaml_path.isEmpty()) {
			is_root_class = true;
			parent_yaml_path = "";
		} else {
			parent_yaml_path += ".";
		}

		for (Field field : fields) {
			SendLog.debug(field.getName() + " | " + field.getType() + " | " + field.getGenericType());
			// アノテーション付き, finalフィールド(メンバー)を弾く。
			if (field.isAnnotationPresent(YamlFileExceptField.class)
					|| Modifier.isFinal(field.getModifiers())) continue;

			// TODO: テスト必要。
			yaml_path_access = parent_yaml_path + field.getName();

			if (field.getType().equals(String.class)) {
				SendLog.debug("String: " + yaml_path_access);
				object = file_object.getFileConfiguration().getString(yaml_path_access);
				setField(parent, field, object);
			} else if (field.getType().equals(int.class)) {
				SendLog.debug("int: " + yaml_path_access);
				object = file_object.getFileConfiguration().getInt(yaml_path_access);
				setField(parent, field, object);
			} else if (field.getType().equals(double.class)) {
				SendLog.debug("double: " + yaml_path_access);
				object = file_object.getFileConfiguration().getDouble(yaml_path_access);
				setField(parent, field, object);
			} else if (field.getType().equals(boolean.class)) {
				SendLog.debug("boolean: " + yaml_path_access);
				object = file_object.getFileConfiguration().getBoolean(yaml_path_access);
				setField(parent, field, object);
			} else if (field.getType().equals(List.class)) {
				if (field.getGenericType() instanceof ParameterizedType) {
					ParameterizedType ptype = (ParameterizedType) field.getGenericType();
					SendLog.debug("List<String>: " + yaml_path_access);
					SendLog.debug(ptype.getTypeName() + " | " + ptype.getRawType());
					for (Type ptype_arg : ptype.getActualTypeArguments()) {
						SendLog.debug("pType arg: " + ptype_arg.getTypeName());
						if (ptype_arg.equals(String.class)) {
							object = file_object.getFileConfiguration().getStringList(yaml_path_access);
							setField(parent, field, object);
						}
					}
				} else {
					SendLog.debug(field.getName() + " is not Parametreized List.");
				}
			} else {
				SendLog.debug(field.getName() + " is not value.");

				// 値型でなかったら、新しいインスタンスのオブジェクトをつくり、そのフィールドを再帰的にこのメソッドを用いて取得→代入。
				Object field_object = null;
				try {
					field_object = field.getType().getConstructor(file_object.getClass()).newInstance(file_object);
				} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
					e.printStackTrace();
				}
				// 再帰的呼び出し
				loadFields(file_object, field_object, yaml_path_access);

				// フィールドを代入されたオブジェクトを代入。
				setField(file_object, field, field_object);
				SendLog.debug(field.getName() + " has been set.");
			}
		}
		if (is_root_class) file_object.loadField(); // TODO: 正しい動作をするにはどうすればよいか。
		return file_object;
	}

	private static void setField(Object dest_class_object, Field field, Object set_object) {
		if (dest_class_object == null || field == null) throw new IllegalArgumentException("agrument is null.");
		try {
			field.set(dest_class_object, set_object);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static void saveYamlFile(YamlFile file) {
		SendLog.send(file.getFileName() + " save.");
		saveFields(file, file, null);
	}

	/**
	 * @param file_object
	 * @param parent
	 * @param parent_yaml_path
	 */
	private static void saveFields(YamlFile file_object, Object parent, String parent_yaml_path) {
		boolean is_root_class = false;
		String yaml_path_access;
		if (file_object == null || parent == null) {
			SendLog.debug("Which objects are null.");
			return;
		}
		Field[] fields = parent.getClass().getFields();
		if (parent_yaml_path == null || parent_yaml_path.isEmpty()) {
			is_root_class = true;
			parent_yaml_path = "";
		} else {
			parent_yaml_path += ".";
		}

		for (Field field : fields) {
			SendLog.debug("Field: " + field.getName());
			// アノテーション付き, finalフィールド(メンバー)を弾く。, final除去は謎フィールドを防ぐ(Memderのフィールド)。
			if (field.isAnnotationPresent(YamlFileExceptField.class)
					|| Modifier.isFinal(field.getModifiers())) continue;

			yaml_path_access = parent_yaml_path + field.getName();

			if (isFieldValueType(field)) {
				try {
					SendLog.debug("Path: " + yaml_path_access);
					Object object = null;
					if (field != null && (object = field.get(parent)) != null) {
						SendLog.debug("Data: " + field.get(parent).toString());
						file_object.getFileConfiguration().set(yaml_path_access, object);
					} else {
						SendLog.debug("Data: null");
						file_object.getFileConfiguration().set(yaml_path_access, "");
					}
				} catch (IllegalArgumentException | IllegalAccessException ex) {
					ex.printStackTrace();
				}
			} else if (field.getType().equals(List.class)) {
				// TODO
				if (field.getGenericType() instanceof ParameterizedType) {
					ParameterizedType ptype = (ParameterizedType) field.getGenericType();
					SendLog.debug("List<String>: " + yaml_path_access);
					SendLog.debug(ptype.getTypeName() + " | " + ptype.getRawType());
					for (Type ptype_arg : ptype.getActualTypeArguments()) {
						SendLog.debug("pType arg: " + ptype_arg.getTypeName());
						if (ptype_arg.equals(String.class)) {
							try {
								SendLog.debug("Path: " + yaml_path_access);
								Object object = null;
								if (field != null && (object = field.get(parent)) != null) {
									SendLog.debug("Data(List<String>): " + object);
									file_object.getFileConfiguration().set(yaml_path_access, object);
								} else {
									// Maybe don't need this.
									SendLog.debug("Data: null");
									file_object.getFileConfiguration().set(yaml_path_access, new ArrayList<>());
								}
							} catch (IllegalArgumentException | IllegalAccessException ex) {
								ex.printStackTrace();
							}
						}
					}
				}
			} else {
				try {
					saveFields(file_object, field.get(file_object), yaml_path_access);
				} catch (IllegalArgumentException | IllegalAccessException ex) {
					ex.printStackTrace();
				}
			}
		}

		if (is_root_class) file_object.saveField();

		try {
			file_object.getFileConfiguration().save(file_object.getFile());
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	private static boolean isFieldValueType(Field field) {
		if (field.getType().equals(String.class)
				|| field.getType().equals(int.class)
				|| field.getType().equals(double.class)
				|| field.getType().equals(float.class)
				|| field.getType().equals(boolean.class)) return true;
		else return false;
	}
}