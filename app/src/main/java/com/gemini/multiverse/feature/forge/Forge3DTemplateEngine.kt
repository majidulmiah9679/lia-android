package com.gemini.multiverse.feature.forge

/**
 * Procedural synthesis engine for interactive 3D WebGL websites using Three.js.
 */
object Forge3DTemplateEngine {

    /**
     * Generates a complete standalone Three.js HTML document from a conversational prompt.
     */
    fun synthesize3DWebsite(prompt: String, title: String): String {
        val safeTitle = if (title.isBlank()) "3D Interactive Experience" else title
        val isCafe = prompt.lowercase().contains("cafe") || prompt.lowercase().contains("coffee")
        val isSpace = prompt.lowercase().contains("space") || prompt.lowercase().contains("solar") || prompt.lowercase().contains("galaxy")

        val accentColor = if (isCafe) "0xff9e00" else if (isSpace) "0x00f0ff" else "0xff007f"
        val sceneName = if (isCafe) "☕ Cyber Cafe 3D" else if (isSpace) "🪐 Interstellar Portal" else "⚡ Matrix Forge"

        return """
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
    <title>$safeTitle</title>
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            background: #0A0A1F;
            color: #F8FAFC;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            overflow: hidden;
            width: 100vw;
            height: 100vh;
        }
        #canvas-container {
            position: absolute;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            z-index: 1;
        }
        .overlay-ui {
            position: absolute;
            top: 24px;
            left: 20px;
            right: 20px;
            z-index: 10;
            pointer-events: none;
        }
        .glass-card {
            background: rgba(18, 18, 43, 0.75);
            backdrop-filter: blur(16px);
            border: 1px solid rgba(255, 158, 0, 0.35);
            border-radius: 16px;
            padding: 18px;
            display: inline-block;
            box-shadow: 0 8px 32px rgba(0, 0, 0, 0.4);
        }
        h1 {
            font-size: 20px;
            font-weight: 700;
            color: #FF9E00;
            margin-bottom: 6px;
        }
        p {
            font-size: 13px;
            color: #94A3B8;
        }
        .controls-hint {
            position: absolute;
            bottom: 24px;
            left: 50%;
            transform: translateX(-50%);
            z-index: 10;
            background: rgba(10, 10, 31, 0.8);
            border: 1px solid rgba(79, 70, 229, 0.4);
            border-radius: 20px;
            padding: 8px 16px;
            font-size: 12px;
            color: #00F0FF;
            pointer-events: none;
        }
    </style>
    <!-- Three.js + OrbitControls -->
    <script src="https://cdnjs.cloudflare.com/ajax/libs/three.js/r128/three.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/three@0.128.0/examples/js/controls/OrbitControls.js"></script>
</head>
<body>
    <div id="canvas-container"></div>
    <div class="overlay-ui">
        <div class="glass-card">
            <h1>$sceneName</h1>
            <p>$prompt</p>
        </div>
    </div>
    <div class="controls-hint">👆 Touch &amp; Drag to rotate • Pinch to zoom</div>

    <script>
        const container = document.getElementById('canvas-container');
        const scene = new THREE.Scene();
        scene.fog = new THREE.FogExp2(0x0A0A1F, 0.035);

        const camera = new THREE.PerspectiveCamera(60, window.innerWidth / window.innerHeight, 0.1, 1000);
        camera.position.set(0, 3, 7);

        const renderer = new THREE.WebGLRenderer({ antialias: true, alpha: true });
        renderer.setSize(window.innerWidth, window.innerHeight);
        renderer.setPixelRatio(Math.min(window.devicePixelRatio, 2));
        container.appendChild(renderer.domElement);

        const controls = new THREE.OrbitControls(camera, renderer.domElement);
        controls.enableDamping = true;
        controls.dampingFactor = 0.05;

        // Ambient & Spot Lights
        const ambientLight = new THREE.AmbientLight(0xffffff, 0.6);
        scene.add(ambientLight);

        const spotLight = new THREE.SpotLight($accentColor, 2.5);
        spotLight.position.set(5, 10, 7);
        spotLight.angle = Math.PI / 4;
        spotLight.penumbra = 0.8;
        scene.add(spotLight);

        // Core 3D Geometry
        const coreGroup = new THREE.Group();
        scene.add(coreGroup);

        // Central Torus Knot / Sculpted Object
        const geometry = new THREE.TorusKnotGeometry(1.4, 0.38, 128, 32);
        const material = new THREE.MeshPhysicalMaterial({
            color: $accentColor,
            metalness: 0.85,
            roughness: 0.15,
            clearcoat: 1.0,
            clearcoatRoughness: 0.1,
            wireframe: false
        });
        const mainMesh = new THREE.Mesh(geometry, material);
        coreGroup.add(mainMesh);

        // Surrounding Particle Vortex
        const particleCount = 600;
        const particleGeometry = new THREE.BufferGeometry();
        const positions = new Float32Array(particleCount * 3);
        for (let i = 0; i < particleCount * 3; i += 3) {
            const radius = 3.5 + Math.random() * 4.0;
            const theta = Math.random() * Math.PI * 2;
            positions[i] = Math.cos(theta) * radius;
            positions[i + 1] = (Math.random() - 0.5) * 4.0;
            positions[i + 2] = Math.sin(theta) * radius;
        }
        particleGeometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
        const particleMaterial = new THREE.PointsMaterial({
            color: 0x00F0FF,
            size: 0.06,
            transparent: true,
            opacity: 0.75
        });
        const particleSystem = new THREE.Points(particleGeometry, particleMaterial);
        scene.add(particleSystem);

        // Window resize
        window.addEventListener('resize', () => {
            camera.aspect = window.innerWidth / window.innerHeight;
            camera.updateProjectionMatrix();
            renderer.setSize(window.innerWidth, window.innerHeight);
        });

        // Animation Loop
        let clock = new THREE.Clock();
        function animate() {
            requestAnimationFrame(animate);
            const delta = clock.getDelta();
            const time = clock.getElapsedTime();

            coreGroup.rotation.y += 0.45 * delta;
            coreGroup.rotation.x = Math.sin(time * 0.5) * 0.2;
            particleSystem.rotation.y -= 0.15 * delta;

            controls.update();
            renderer.render(scene, camera);
        }
        animate();
    </script>
</body>
</html>
        """.trimIndent()
    }
}
