CLANG_FORMAT=node_modules/clang-format/bin/linux_x64/clang-format --style=Google
CSS_VALIDATOR=node_modules/css-validator/bin/css-validator
ESLINT=node_modules/eslint/bin/eslint.js
HTML_VALIDATE=node_modules/html-validate/bin/html-validate.js
PRETTIER=node_modules/prettier/bin-prettier.js

node_modules:
	npm install clang-format prettier css-validator html-validate eslint eslint-config-google

pretty: node_modules
	$(PRETTIER) --write launchpod/angular-launchpod/src/app/*.component.{html,css}
	find launchpod/angular-launchpod/backend/src/main/java -iname *.java | xargs $(CLANG_FORMAT) -i
	find launchpod/angular-launchpod/src/app/*.component.ts | xargs $(CLANG_FORMAT) -i

package:
	mvn package
