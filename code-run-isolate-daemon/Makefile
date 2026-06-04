.PHONY: all clean

all: code-run-isolate-daemon

code-run-isolate-daemon: main.go
	docker run --rm -v $$(pwd):/app -w /app golang:1.22-alpine go build -o code-run-isolate-daemon .

clean:
	rm -f code-run-isolate-daemon
