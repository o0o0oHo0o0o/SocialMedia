import React, { useEffect, useRef } from 'react';
import * as THREE from 'three';

const GalaxyBackground = ({ isDark }) => {
	const mountRef = useRef(null);
	const sceneRef = useRef(null);
	const rendererRef = useRef(null);
	const particlesRef = useRef(null);
	const materialRef = useRef(null);

	useEffect(() => {
		if (!mountRef.current) return;

		const width = mountRef.current.clientWidth || window.innerWidth;
		const height = mountRef.current.clientHeight || window.innerHeight;

		const scene = new THREE.Scene();
		sceneRef.current = scene;

		const camera = new THREE.PerspectiveCamera(75, width / height, 0.1, 1000);
		camera.position.z = 5;

		const renderer = new THREE.WebGLRenderer({ alpha: true, antialias: true });
		rendererRef.current = renderer;
		const dpr = Math.min(window.devicePixelRatio || 1, 2);
		renderer.setPixelRatio(dpr);
		renderer.setSize(width, height);
		renderer.domElement.style.position = 'absolute';
		renderer.domElement.style.top = '0';
		renderer.domElement.style.left = '0';
		renderer.domElement.style.width = '100%';
		renderer.domElement.style.height = '100%';
		renderer.domElement.style.pointerEvents = 'none';
		mountRef.current.appendChild(renderer.domElement);

		// adaptive particle count (balanced for performance)
		const area = Math.max(1, width * height);
		const particlesCount = Math.max(600, Math.min(3000, Math.floor(area / 1500)));

		const positions = new Float32Array(particlesCount * 3);
		const colors = new Float32Array(particlesCount * 3);

		for (let i = 0; i < particlesCount; i++) {
			const i3 = i * 3;
			positions[i3] = (Math.random() - 0.5) * 20;
			positions[i3 + 1] = (Math.random() - 0.5) * 20;
			positions[i3 + 2] = (Math.random() - 0.5) * 20;

					const c = new THREE.Color();
					if (isDark) {
						c.setHSL(0.6 + Math.random() * 0.25, 0.6, 0.55 + Math.random() * 0.15);
					} else {
						c.setHSL(0.58 + Math.random() * 0.15, 0.8, 0.85 + Math.random() * 0.1);
					}
			colors[i3] = c.r;
			colors[i3 + 1] = c.g;
			colors[i3 + 2] = c.b;
		}

		const geometry = new THREE.BufferGeometry();
		geometry.setAttribute('position', new THREE.BufferAttribute(positions, 3));
		geometry.setAttribute('color', new THREE.BufferAttribute(colors, 3));

			const particleSize = isDark ? (dpr > 1.5 ? 0.045 : 0.035) : (dpr > 1.5 ? 0.08 : 0.07);
			const material = new THREE.PointsMaterial({
				size: particleSize,
				vertexColors: true,
				transparent: true,
				opacity: isDark ? 0.9 : 0.6,
				blending: isDark ? THREE.AdditiveBlending : THREE.NormalBlending,
			});
		materialRef.current = material;

		const particles = new THREE.Points(geometry, material);
		particlesRef.current = particles;
		scene.add(particles);

		const ambient = new THREE.AmbientLight(0xffffff, 0.12);
		scene.add(ambient);

		let rafId = null;
		const animate = () => {
			rafId = requestAnimationFrame(animate);
			if (particlesRef.current) {
				particlesRef.current.rotation.y += 0.0006;
				particlesRef.current.rotation.x += 0.00025;
			}
			renderer.render(scene, camera);
		};
		animate();

		const ro = new ResizeObserver((entries) => {
			const rect = entries[0].contentRect;
			const w = rect.width || window.innerWidth;
			const h = rect.height || window.innerHeight;
			camera.aspect = w / h;
			camera.updateProjectionMatrix();
			renderer.setPixelRatio(Math.min(window.devicePixelRatio || 1, 2));
			renderer.setSize(w, h);
		});
		ro.observe(mountRef.current);

		return () => {
			ro.disconnect();
			if (rafId) cancelAnimationFrame(rafId);
			try { if (mountRef.current && renderer.domElement) mountRef.current.removeChild(renderer.domElement); } catch (e) {}
			// dispose geometry/material/renderer
			geometry.dispose();
			material.dispose();
			renderer.dispose();
			particlesRef.current = null;
			materialRef.current = null;
			rendererRef.current = null;
			sceneRef.current = null;
		};
	}, [isDark]);

	return (
		<div
			ref={mountRef}
			className="galaxy-background"
			style={{
				position: 'fixed',
				top: 0,
				left: 0,
				width: '100%',
				height: '100%',
				zIndex: 0,
				background: isDark
					? 'linear-gradient(135deg, #0f0c29 0%, #302b63 50%, #24243e 100%)'
					: 'linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%)',
				transition: 'background 0.5s ease',
				overflow: 'hidden',
			}}
		/>
	);
};

export default GalaxyBackground;

