#!/usr/bin/env bash
# Компилирует каждую работу и запускает её SelfCheck; для №14 — mvn test.
set -u
cd "$(dirname "$0")"
status=0
for dir in pr03-04 pr05 pr06 pr07 pr08 pr09 pr10 pr11 pr12 pr13 pr15 pr16; do
    printf '%-8s ' "$dir"
    if (cd "$dir" && rm -rf out && javac -encoding UTF-8 -d out src/*.java && java -cp out SelfCheck > /tmp/selfcheck_$dir.log 2>&1); then
        tail -1 "/tmp/selfcheck_$dir.log"
    else
        echo "ОШИБКА (подробности: /tmp/selfcheck_$dir.log)"
        status=1
    fi
done
printf '%-8s ' "pr14"
if (cd pr14 && mvn -q -B test > /tmp/selfcheck_pr14.log 2>&1); then
    echo "mvn test: все тесты пройдены"
else
    echo "mvn test: ОШИБКА (подробности: /tmp/selfcheck_pr14.log)"
    status=1
fi
exit $status
